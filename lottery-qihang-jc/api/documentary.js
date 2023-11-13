import request from '@/util/ajax'

/**
 * 创建跟单
 * @param {Object} data
 */
export function createDocumentary(data) {
	return request({
		url: '/app/documentary/create',
		method: 'post',
		data
	})
}

export function getLottery(type) {
	const lotterys = {
		0: '竞彩足球',
		1: '竞彩篮球',
		2: '北京单场',
		3: '排列3',
		4: '排列5',
		5: '七星彩',
		6: '足球14场',
		7: '任选九',
		8: '大乐透',
		21: '福彩3D',
		22: '七乐彩',
		23: '快乐8',
		24: '双色球'
	}
	const lotteryName = lotterys[type]
	if (typeof(lotteryName) == 'undefined' || lotteryName == null) {
		return ''
	}
	return lotteryName

}
/**
 * 跟单排行榜
 */
export function ranking() {
	return request({
		url: '/app/documentary/ranking',
		method: 'get',
	})
}

export function documentaryDetails(id) {
	return request({
		url: '/app/documentary/details/' + id,
		method: 'get',
	})
}

export function queryDocumentaryByType(type) {
	return request({
		url: '/app/documentary/by/' + type,
		method: 'get',
	})
}

export function queryDocumentaryById(id, userId) {
	return request({
		url: '/app/documentary/get/' + id + "/" + userId,
		method: 'get',
	})
}

export function createDocumentaryUser(data) {
	return request({
		url: '/app/documentary/add',
		method: 'post',
		data
	})
}