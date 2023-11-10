import request from '@/util/ajax'

/**
 * 店铺列表
 */
export function shopList(data) {
	return request({
		url: '/app/shop/list',
		method: 'post',
		data
	})
}




/* ;
: */