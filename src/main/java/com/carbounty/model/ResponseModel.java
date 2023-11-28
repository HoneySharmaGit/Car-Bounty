package com.carbounty.model;

import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONObject;

public class ResponseModel {

	private Object data;
	private String message;
	private String status;

	public ResponseModel(Object ob, String message, String status) {
		this.data = ob;
		this.message = message;
		this.status = status;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public JSONObject convertToJson() {
		Map<String, Object> map = new HashMap<>();
		map.put("data", this.data);
		map.put("message", this.message);
		map.put("status", this.status);
		JSONObject json = new JSONObject(map);
		return json;
	}

}
