import request from "@/utils/request";

// 查询用户列表
export function getUserList(data) {
  return request({
    url: "/admin/user/list",
    method: "post",
    data: data,
  });
}

// 添加用户
export function addUser(data) {
  return request({
    url: "/admin/user/add",
    method: "post",
    data: data,
  });
}

// 删除用户
export function deleteUser(userId) {
  return request({
    url: "/admin/user/delete/" + userId,
    method: "delete",
  });
}

// 设置代理和启用/禁用用户
export function updateUser(userId, data) {
  console.log(userId);
  console.log(data);

  return request({
    url: "/admin/user/update/" + userId,
    method: "put",
    data: data,
  });
}

// 修改用户密码
export function updateUserPwd(data) {
  return request({
    url: "/admin/user/change/pwd",
    method: "put",
    data: data,
  });
}

// 验证用户交易密码
export function verifyUserPwd(data) {
  return request({
    url: "/sys/user/verify",
    method: "post",
    data: data,
  });
}

// 充值接口
export function rechargeUser(userId, data) {
  return request({
    url: "/admin/user/recharge/" + userId,
    method: "put",
    data: data,
  });
}

// ---------------- 客户统计 -----------------------
export function userStatistics(data) {
  return request({
    url: "/admin/user/statistics",
    method: "post",
    data: data,
  });
}

// ---------------- 系统用户 -----------------------
// 添加系统用户
export function addSysUser(data) {
  return request({
    url: "/sys/user/add",
    method: "post",
    data: data,
  });
}

// 系统用户列表
export function getSysList(data) {
  return request({
    url: "/sys/user/list",
    method: "post",
    data: data,
  });
}

// 删除系统用户
export function deleteSysUser(userId) {
  return request({
    url: "/sys/user/delete/" + userId,
    method: "delete",
  });
}
