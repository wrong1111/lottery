import request from "@/utils/request";

// 彩种列表展示
export function getRemoteLotteryInfo(data) {
  return request({
    url: "/admin/transfer/change/info",
    method: "post",
    data:data
  });
}
// 转单 彩种列表
export function getChangeList(data) {
  return request({
    url: "/admin/transfer/change/list",
    method: "post",
    data:data
  });
}

export function getShopall() {
  return request({
    url: "/admin/transfer/change/shopall",
    method: "get",
  });
}


export function editAutoState(data) {
  return request({
    url: "/admin/transfer/change/editAutoState",
    method: "post",
    data:data
  });
}


export function editDisable(data) {
  return request({
    url: "/admin/transfer/change/editDisable",
    method: "post",
    data:data
  });
}
