import request from "@/utils/request";

// 获取流水订单
export function getPayOrderList(data) {
  return request({
    url: "/admin/pay/order/list",
    method: "post",
    data: data,
  });
}

// 体彩订单
export function getLottryOrderList(data) {
  return request({
    url: "/admin/lottery/order/list",
    method: "post",
    data: data,
  });
}

// 派奖
export function orderAward(data) {
  return request({
    url: "/admin/lottery/order/award",
    method: "put",
    data: data,
  });
}

// 一键出票
export function orderTicketing(data) {
  return request({
    url: "/admin/lottery/order/ticketing",
    method: "put",
    data: data,
  });
}

// 退票
export function orderRetreat(id) {
  return request({
    url: "/admin/lottery/order/retreat/" + id,
    method: "put",
  });
}

//上传票据

export function orderActual(data) {
  return request({
    url: '/admin/lottery/order/actual',
    method: 'put',
    data: data
  })
}
