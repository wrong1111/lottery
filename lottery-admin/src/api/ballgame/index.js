import request from "@/utils/request";

// 栏目设置列表
export function getBallList() {
  return request({
    url: "/admin/ball/list",
    method: "get",
  });
}

// 栏目上/下架（0:上架 1:下架）
export function updateBallLine(id, status) {
  return request({
    url: "/admin/ball/update/line/" + id + "/" + status,
    method: "put",
  });
}

// 公告设置列表
export function getNoticeList() {
  return request({
    url: "/admin/notice/list",
    method: "get",
  });
}

// 公告修改
export function updateNotice(id, data) {
  return request({
    url: "/admin/notice/update/" + id,
    method: "put",
    data: data,
  });
}

// 域名查询
export function getDomainList() {
  return request({
    url: "/admin/domain/query",
    method: "get",
  });
}

// 公告修改
export function updateDomain(id, data) {
  return request({
    url: "/admin/domain/update/" + id,
    method: "put",
    data: data,
  });
}
