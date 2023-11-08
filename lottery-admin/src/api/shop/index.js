import request from "@/utils/request";

// 查询店铺列表
export function getShopList(data) {
  return request({
    url: "/admin/shop/list",
    method: "post",
    data: data,
  });
}

// 添加店铺
export function addShop(data) {
  return request({
    url: "/admin/shop/add",
    method: "post",
    data: data,
  });
}

// 删除店铺
export function deleteShop(shopId) {
  return request({
    url: "/admin/shop/delete/" + shopId,
    method: "delete",
  });
}

// 支付配置
export function updateShopPay(data) {
  return request({
    url: "/admin/dict/insert",
    method: "put",
    data: data,
  });
}

// 店铺上/下架 0:上架 1:下架
export function updateShopLine(shopId, status) {
  return request({
    url: "/admin/shop/update/line/" + shopId + "/" + status,
    method: "put",
  });
}

// 店铺充值接口
export function rechargeShop(data) {
  return request({
    url: "/admin/shop/recharge",
    method: "post",
    data: data,
  });
}

// 阿里支付配置
export function getAliPayConfig(shopId) {
  return request({
    url: "/admin/dict/get/" + shopId,
    method: "get",
  });
}

// 查询店铺用户列表
export function getUserList(shopId) {
  return request({
    url: "/sys/user/get/" + shopId,
    method: "get",
  });
}

