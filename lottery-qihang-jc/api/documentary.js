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
		24: '双色球',
		25:'北京单场-胜负过关',
	}
	const lotteryName = lotterys[type]
	if (typeof(lotteryName) == 'undefined' || lotteryName == null) {
		return ''
	}
	return lotteryName

}
export function decisionSport(type) {
	switch (type) {
		case "0":
		case "1":
		case "2":
		case "6":
		case "7":
		case "25":
			return true
		default:
			return false
	}

}

export function getPlayType(lotid, mode) {
	if (23 == lotid) {
		return playType23(mode)
	}
	if (mode == "0") {
		return "直选";
	} else if (mode == "1") {
		return "组三";
	} else if (mode == "2") {
		return "组六";
	} else if (mode == "3") {
		return "直选和值";
	} else if (mode == "4") {
		return "组选和值";
	} else if (mode == "5") {
		return "组三复式";
	}
}

export function playType23(mode) {
	if (mode == "10") {
		return "选十"
	} else if (mode == "9") {
		return "选九"
	} else if (mode == "8") {
		return "选八"
	} else if (mode == "7") {
		return "选七"
	} else if (mode == "6") {
		return "选六"
	} else if (mode == "5") {
		return "选五"
	} else if (mode == "4") {
		return "选四"
	} else if (mode == "3") {
		return "选三"
	} else if (mode == "2") {
		return "选二"
	} else if (mode == "1") {
		return "选一"
	}
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