import request from '@/util/ajax'


/*
发起跟单

*/
export function documentaryDigit(data,issueNo){
	uni.navigateTo({
		url: "pages/documentary/placeDigit/placeDigit?obj=" + encodeURIComponent(JSON.stringify(data))+"&issueNo="+issueNo
	});
}

/**
 * 排列下单
 * @param {Object} data
 */
export function place(data,type,issueNo) {
	return request({
		url: '/app/permutation/place/'+type+"/"+issueNo,
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