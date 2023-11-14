import request from '@/util/ajax'


/*
发起跟单

*/
export function documentaryDigit(data){
	uni.navigateTo({
		url: "pages/documentary/placeDigit/placeDigit?obj=" + encodeURIComponent(JSON.stringify(data))
	});
}

/**
 * 排列下单
 * @param {Object} data
 */
export function place(data,type) {
	return request({
		url: '/app/permutation/place/'+type,
		method: 'post',
		data
	})
}

/**
 * 历史排列开奖记录
 */
export function record(type) {
	return request({
		url: '/app/permutation/record/'+type,
		method: 'get',
	})
}

/**
 * 获取排列期号接口
 */
export function getIssueNo(type) {
	return request({
		url: '/app/permutation/issue/'+type,
		method: 'get',
	})
}