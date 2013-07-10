/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2012 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.jaamsim.render;

import java.util.ArrayList;
import java.util.Map;

import com.jaamsim.MeshFiles.MeshData;
import com.jaamsim.math.AABB;
import com.jaamsim.math.Color4d;
import com.jaamsim.math.Mat4d;
import com.jaamsim.math.MathUtils;
import com.jaamsim.math.Ray;
import com.jaamsim.math.Transform;
import com.jaamsim.math.Vec3d;
import com.jaamsim.math.Vec4d;

//import javax.media.opengl.*;

public class Mesh implements Renderable {

private MeshProto _proto;
private MeshProtoKey _key;
private Transform _trans;
private Vec4d _scale; // Allow for a non-uniform scale

private long _pickingID;
private VisibilityInfo _visInfo;

private AABB _bounds;

private Mat4d _modelMat;
private Mat4d _normalMat;
private ArrayList<AABB> _subMeshBounds;
private ArrayList<Action.Queue> _actions;

public Mesh(MeshProtoKey key, MeshProto proto, Transform trans,
            ArrayList<Action.Queue> actions, VisibilityInfo visInfo, long pickingID) {
	this(key, proto, trans, new Vec4d(0, 0, 0, 1.0d), actions, visInfo, pickingID);
}

public Mesh(MeshProtoKey key, MeshProto proto, Transform trans, Vec4d scale,
            ArrayList<Action.Queue> actions, VisibilityInfo visInfo, long pickingID) {

	_trans = new Transform(trans);
	_proto = proto;
	_scale = new Vec4d(scale);
	_key = key;
	_visInfo = visInfo;
	_actions = actions;

	_modelMat = RenderUtils.mergeTransAndScale(_trans, _scale);

	_normalMat = RenderUtils.getInverseWithScale(_trans, _scale);
	_normalMat.transpose4();

	_bounds = _proto.getHull().getAABB(_modelMat);

	_subMeshBounds = _proto.getSubBounds(_modelMat);

	_pickingID = pickingID;
}

@Override
public AABB getBoundsRef() {
	return _bounds;
}

@Override
public void render(Map<Integer, Integer> vaoMap, Renderer renderer, Camera cam, Ray pickRay) {

	_proto.render(vaoMap, renderer, _modelMat, _normalMat, cam, _actions, _subMeshBounds);

	// Debug render of the convex hull
	if (renderer.debugDrawHulls()) {

		Mat4d modelViewMat = new Mat4d();
		cam.getViewMat4d(modelViewMat);
		modelViewMat.mult4(_modelMat);

		ConvexHullKey hullKey = new ConvexHullKey(_key);
		HullProto hp = renderer.getHullProto(hullKey);
		if (hp != null) {
			hp.render(vaoMap, renderer, modelViewMat, cam);
		}
	}
	if (renderer.debugDrawArmatures()) {
		Mat4d modelViewMat = new Mat4d();
		cam.getViewMat4d(modelViewMat);
		modelViewMat.mult4(_modelMat);

		MeshData md = _proto.getRawData();
		for (Armature arm : md.getArmatures()) {
			ArrayList<Mat4d> pose = null;
			if (_actions != null) {
				pose = arm.getPose(_actions);
			}
			DebugUtils.renderArmature(vaoMap, renderer, modelViewMat, arm, pose, new Color4d(1, 0, 0), cam);
		}
	}
}

@Override
public long getPickingID() {
	return _pickingID;
}

@Override
public double getCollisionDist(Ray r, boolean precise)
{
	double boundsDist = _bounds.collisionDist(r);
	if (boundsDist < 0) {
		return boundsDist;
	}

	double roughCollision = _proto.getHull().collisionDistance(r, _trans, _scale);
	if (!precise || roughCollision < 0) {
		// This is either a rough collision, or we missed outright
		return roughCollision;
	}

	double shortDistance = Double.POSITIVE_INFINITY;

	MeshData data = _proto.getRawData();
	// Check against all sub meshes
	for (int instInd = 0; instInd < data.getSubMeshInstances().size(); ++instInd) {
		AABB subBounds = _subMeshBounds.get(instInd);
		// Rough collision to AABB
		if (subBounds.collisionDist(r) < 0) {
			continue;
		}

		MeshData.SubMeshInstance subInst = data.getSubMeshInstances().get(instInd);

		MeshData.SubMeshData subData = data.getSubMeshData().get(subInst.subMeshIndex);

		Mat4d subMat = RenderUtils.mergeTransAndScale(_trans, _scale);
		subMat.mult4(subInst.transform);

		Mat4d invMat = subMat.inverse();
		double subDist = subData.hull.collisionDistanceByMatrix(r, subMat, invMat);
		if (subDist < 0) {
			continue;
		}
		// We have hit both the AABB and the convex hull for this sub instance, now do individual triangle collision

		Ray localRay = r.transform(invMat);
		Vec4d[] triVecs = new Vec4d[3];

		for (int triInd = 0; triInd < subData.indices.length / 3; ++triInd) {
			triVecs[0] = subData.verts.get(subData.indices[triInd*3+0]);
			triVecs[1] = subData.verts.get(subData.indices[triInd*3+1]);
			triVecs[2] = subData.verts.get(subData.indices[triInd*3+2]);
			if ( triVecs[0].equals4(triVecs[1]) ||
			     triVecs[1].equals4(triVecs[2]) ||
			     triVecs[2].equals4(triVecs[0])) {
				continue;
			}
			double triDist = MathUtils.collisionDistPoly(localRay, triVecs);
			if (triDist > 0) {
				// We have collided, now we need to figure out the distance in original ray space, not the transformed ray space
				Vec3d temp = localRay.getPointAtDist(triDist);
				temp.multAndTrans3(subMat, temp); // Temp is the collision point in world space
				temp.sub3(temp, r.getStartRef());

				double newDist = temp.mag3();

				if (newDist < shortDistance) {
					shortDistance = newDist;
				}
			}
		}
	}

	// Now check against line components
	for (int instInd = 0; instInd < data.getSubLineInstances().size(); ++instInd) {
		MeshData.SubLineInstance subInst = data.getSubLineInstances().get(instInd);

		MeshData.SubLineData subData = data.getSubLineData().get(subInst.subLineIndex);

		Mat4d subMat = RenderUtils.mergeTransAndScale(_trans, _scale);
		subMat.mult4(subInst.transform);

		Mat4d invMat = subMat.inverse();
		double subDist = subData.hull.collisionDistanceByMatrix(r, subMat, invMat);
		if (subDist < 0) {
			continue;
		}

		Mat4d rayMat = MathUtils.RaySpace(r);
		Vec4d[] lineVerts = new Vec4d[subData.verts.size()];
		for (int i = 0; i < lineVerts.length; ++i) {
			lineVerts[i] = new Vec4d();
			lineVerts[i].mult4(subMat, subData.verts.get(i));
		}

		double lineDist = MathUtils.collisionDistLines(rayMat, lineVerts, 0.01309); // Angle is 0.75 deg in radians

		if (lineDist > 0 && lineDist < shortDistance) {
			shortDistance = lineDist;
		}
	}

	if (shortDistance == Double.POSITIVE_INFINITY) {
		return -1; // We did not actually collide with anything
	}

	return shortDistance;

}

@Override
public boolean hasTransparent() {
	return _proto.hasTransparent();
}

@Override
public void renderTransparent(Map<Integer, Integer> vaoMap, Renderer renderer, Camera cam, Ray pickRay) {

	// TODO: pass actions here
	_proto.renderTransparent(vaoMap, renderer, _modelMat, _normalMat, cam, _actions, _subMeshBounds);

}

@Override
public boolean renderForView(int viewID, double dist) {
	return _visInfo.isVisible(viewID, dist);
}
}
