import request from "@/utils/request";

export function login(data) {
  return request({
    url: "/sys/user/login",
    method: "post",
    data,
  });
}

// 获取用户信息接口
export function getInfo() {
  return request({
    url: "/app/user/get",
    method: "get",
  });
}

export function logout() {
  return request({
    url: "/vue-admin-template/user/logout",
    method: "post",
  });
}
