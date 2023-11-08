import request from "@/utils/request";

// 查询操作列表
export function getLogList(data) {
  return request({
    url: "/admin/log/list",
    method: "post",
    data: data,
  });
}
