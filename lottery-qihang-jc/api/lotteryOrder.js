
/* ;
: */

import request from '@/util/ajax'

export function isSport(type){
	if (type == "3" || type == "4" || type == "5" || type == "8" || type == "21" || type == "22" || type == "23" || type == "24") {
	  return false;
	}
	return true;
}
/**
 * 购彩订单记录
 */
export function getLotteryOrderPage(data) {
	return request({
		url: '/app/lottery/order/list',
		method: 'post',
		data
	})
}

/**
 * 根据id订单订单记录
 */
export function getLotteryOrderById(id) {
	return request({
		url: '/app/lottery/order/get/'+id,
		method: 'get',
	})
}

/**
 * 获取昨日下单中奖的订单提示信息
 */
export function centre() {
	return request({
		url: '/app/lottery/order/centre',
		method: 'get',
	})
}