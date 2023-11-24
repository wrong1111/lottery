import request from "@/utils/request";

export function shopTransferList() {
  return request({
    url: "/admin/transferin/list",
    method: "get",
  });
}


export function editShopTransfer(id,data) {
  return request({
    url: "/admin/transferin/edit/"+id,
    method: "POST",
    data:data
  });
}

export function addLotteryTransfer(data) {
  return request({
    url: "/admin/lottery/transferin/add",
    method: "POST",
    data:data
  });
}

export function editState(data){
  return request({
    url: "/admin/lottery/transferin/editState",
    method: "POST",
    data:data
  });
}
export function editCommiss(data){
  return request({
    url: "/admin/lottery/transferin/editCommiss",
    method: "POST",
    data:data
  });
}

export function noOpenLottery(){
  return request({
    url: "/admin/ball/listTransfer",
    method: "get",
  });
}
