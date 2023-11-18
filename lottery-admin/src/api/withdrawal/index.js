import request from "@/utils/request";

// 查询提现列表
export function getWithdrawalList(data) {
  return request({
    url: "/admin/withdrawal/list",
    method: "post",
    data: data,
  });
}

// 处理提现
export function processWithdrawal(data) {
  return request({
    url: "/admin/withdrawal/examine",
    method: "put",
    data: data,
  });
}
