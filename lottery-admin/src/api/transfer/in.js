import request from "@/utils/request";

// 彩种列表展示
export function shopTransferList() {
  return request({
    url: "/admin/transferin/list",
    method: "get",
  });
}

// 修改下游 商家
export function editShopTransfer(data) {
  return request({
    url: "/admin/transferin/plat/edit",
    method: "POST",
    data: data
  });
}

//彩种收单 开通
export function addLotteryTransfer(data) {
  return request({
    url: "/admin/lottery/transferin/add",
    method: "POST",
    data: data
  });
}

// 彩种开通，停止
export function editState(data) {
  return request({
    url: "/admin/lottery/transferin/editState",
    method: "POST",
    data: data
  });
}

// 修改返点
export function editCommiss(data) {
  return request({
    url: "/admin/lottery/transferin/editCommiss",
    method: "POST",
    data: data
  });
}

// 彩种列表
export function noOpenLottery() {
  return request({
    url: "/admin/ball/listTransfer",
    method: "get",
  });
}

// 下游商家
export function platlist() {
  return request({
    url: "/admin/transferin/plat",
    method: "get",
  });
}

//重置 下游 商家秘钥
export function platSecurityReset(id, security) {
  return request({
    url: "/admin/transferin/plat/reset/" + id + "?security=" + security,
    method: "post",
  })
}
//修改下游商家开通状态
export function editPlatState(id, state) {
  console.log(id,state)
  return request({
    url: "/admin/transferin/plat/editState/" + id + "?state=" + state,
    method: "post",
  })
}
