import request from "@/utils/request";

// 查询提现列表
export function getWithdrawalList(data) {
  return request({
    url: "/admin/withdrawal/list",
    method: "post",
    data: data,
  });
}
