<template>
	<view class="box">

		<cmd-nav-bar back title="快乐8" font-color="#fff" background-color="#FF3F43" right-text="快乐8"
			@rightText="rightBtn">
		</cmd-nav-bar>
		<div style="height:100%">
			<p class="fc_index">第{{issueNo}}期，每天一期 开奖</p>
			<view>
				<u-sticky bgColor="#fff"
					style="margin-bottom: 30px;display: flex;justify-content: center;align-items: center;">
					<u-tabs :list="tabs" lineColor="#FF3F43"
						:activeStyle="{color: '#FF3F43',fontWeight: 'bold',transform: 'scale(1.05)'}"
						@click="changeSelectBall($event,1)" :current="zindex"></u-tabs>
				</u-sticky>
				<div class="fc">
					<view
						style="display: flex;justify-content: space-between;align-items: center;width: 95%;margin: 0 auto;">
						<p class="tips"><span class="shake"></span>选择<span>10</span>个号码，中奖<span>5000000</span>元</p>
						<u-checkbox-group @change="checkChange" size="15" shape="square" placement="column"
							style="margin-left: 20px;">
							<u-checkbox labelSize="14" activeColor="#FF3F43" :label="'显示遗漏'">
							</u-checkbox>
						</u-checkbox-group>
					</view>
					<ul>
						<p>号码</p>
						<li @click="check(1,1,index)" v-for="(item,index) in ge" :class="item.active?'active':''">
							{{item.num}}<span style="font-size: 15px;" v-if="item.isGallbladder">胆</span>
							<view v-if="omitData.record!=undefined&&omiIsShow"
								style="color: #A5A5A5;font-size: 13px;margin-top: -8px;">
								{{omitData.record[index]}}
							</view>
							<u-checkbox-group shape="square" @change="checkboxChange($event,index,1)"
								:disabled="redLength>=dan&&item.isGallbladder==false">
								<u-checkbox activeColor="#FF3F43" :name="item.isGallbladder"
									:checked="item.isGallbladder">
								</u-checkbox>
							</u-checkbox-group>
						</li>
					</ul>
				</div>

			</view>
			<Acount :total="total" :acount="acount" @clear="clear" @confirm="sure" />
		</div>

	</view>
</template>
<script>
	import {
		getIssueNo
	} from '@/api/pailie.js'
	import {
		getOmitByType
	} from '@/api/omit.js'
	import Acount from '../common/Acount'
	export default {
		data() {
			return {
				zindex: 0,
				//胆1的长度
				redLength: 0,
				//当前选择的索引
				current: 0,
				total: 0,
				acount: 0,
				issueNo: "",
				dan:9,
				//tab2选项
				tabs: [{
					name: '选十',
				}, {
					name: '选九',
				}, {
					name: '选八',
				}, {
					name: '选七',
				}, {
					name: '选六',
				}, {
					name: '选五',
				}, {
					name: '选四',
				}, {
					name: '选三',
				}, {
					name: '选二',
				}, {
					name: '选一',
				}],
				mode: [{
						name: '选十',
						model: '10',
						dan:9,
						num:16,
					}, {
						name: '选九',
						model: '9'
					}, {
						name: '选八',
						model: '8'
					}, {
						name: '选七',
						model: '7'
					}, {
						name: '选六',
						model: '6'
					},
					{
						name: '选五',
						model: '5'
					}, {
						name: '选四',
						model: '4'
					}, {
						name: '选三',
						model: '3'
					}, {
						name: '选二',
						model: '2'
					}, {
						name: '选一',
						model: '1'
					}
				],
				directlyElectedGentle: [],
				ge: [],
				groupGentle: [],
				groupGentleArr: [],
				omitData: {},
				omiIsShow: false,
			}
		},
		components: {
			Acount
		},
		onLoad() {
			this.init()
		},
		methods: {
			combinationCount(n, m) {
			  // 边界情况
			  if (m === 0 || n === m) {
			    return 1;
			  } else if (m > n) {
			    return 0;
			  }
			
			  // 递归计算组合数
			  return combinationCount(n-1, m - 1) + combinationCount(n-1, m);
			},
			checkChange(item) {
				if (item[0] == "") {
					this.omiIsShow = true;
				} else {
					this.omiIsShow = false;
				}
			},
			checkboxChange(item, index, type) {
				 this.ge[index].isGallbladder = !this.ge[index].isGallbladder;
				 this.gallbladderStatistics(type);
				 if (item[0] != undefined) {
				 	if (type == 1) {
				 		this.ge[index].active = true;
				 	}  
				 }
			},
			calculate(type) {

			},
			gallbladderStatistics(type) {
				let s2 = this.ge.filter(item => {
					return item.isGallbladder;
				})
				this.redLength = s2.length
			},
			//切换事件
			change3(index) {
				let data = uni.getStorageSync('kl8');
				if (data != "") {
					uni.showModal({
						title: '玩家切换提醒',
						content: '不支持直选和组选的混合投注，切换玩法将清空已选择号码，是否仍要切换',
						success: (res => {
							if (res.confirm) {
								uni.removeStorageSync("kl8")
								this.current = index
								this.zindex = 0;
								this.clear();
							}
						}),
					});
				} else {
					this.current = index
					this.zindex = 0;
					this.clear();
				}
			},
			init() {
				getIssueNo("23").then(res => {
					this.issueNo = res.stageNumber
				})
				getOmitByType("23").then(res => {
					this.omitData = res
					if (res.record) {
						this.omitData.record = res.record.split(",")
					}
				})
				this.initNumber()
			},
			initNumber() {
				//初始化80个号码
				for (var i = 1; i <= 80; i++) {
					this.ge.push({
						num: i < 10 ? '0' + i : '' + i,
						active: false,
						isGallbladder: false
					})
				}
			},
			rightBtn() {
				uni.navigateTo({
					url: "/pages/kl8/openPrize"
				})
			},
			check(type, wei = 0, index) {
				switch (type) {
					case 1:
						this.ge[index].active = !this.ge[index].active;
						this.gearr = this.ge.filter(v => {
							return v.active
						})
						this.dan = this.ge.filter(v=>{
							return v.active && v.isGallbladder
						}).length
						
						this.acount = this.globalUtil.math(this.gearr.length, 10)
						break
				}
				this.total = this.acount * 2;
			},

			clear() {
				this.ge.map(v => {
					v.active = false;
					v.isGallbladder = false
				});
				this.redLength = 0;
				this.blueLength = 0;
				this.total = 0
				this.acount = 0
				this.gearr = []
				this.directlyElectedGentleArr = []
				this.groupGentleArr = []
			},
			//tab切换事件
			changeSelectBall(item, type) {
				console.log(' tab ', item)
				this.zindex = item.index
				this.current = item.index
				this.clear();
			},
			//机选
			randomSelect() {
				this.clear();
				if (this.zindex == 0 && this.current == 0) {
					let numberArr = this.globalUtil.randomFromZero(10, 3);
					this.bai[numberArr[0]].active = true
					this.shi[numberArr[1]].active = true
					this.ge[numberArr[2]].active = true
					this.shiarr = this.shi.filter(v => {
						return v.active
					})
					this.baiarr = this.bai.filter(v => {
						return v.active
					})
					this.gearr = this.ge.filter(v => {
						return v.active
					})
					this.acount = this.globalUtil.math(this.baiarr.length, 1) * this.globalUtil.math(this.shiarr.length,
						1) * this.globalUtil.math(
						this.gearr.length, 1)
				} else if (this.zindex == 1 && this.current == 0) {
					let numberArr = this.globalUtil.randomFromZero(28, 1);
					this.directlyElectedGentle[numberArr[0]].active = true
					this.directlyElectedGentleArr = this.directlyElectedGentle.filter(v => {
						return v.active
					})
					this.acount = this.directlyElectedGentle[numberArr[0]].pour;
				} else if (this.zindex == 0 && this.current == 1) {
					let numberArr = this.globalUtil.randomFromZero(10, 2);
					for (var i = 0; i < numberArr.length; i++) {
						this.san[numberArr[i]].active = true
					}
					this.sanarr = this.san.filter(v => {
						return v.active
					});
					this.acount = this.globalUtil.math(this.sanarr.length, 2) * 2;
				} else if (this.zindex == 1 && this.current == 1) {
					let numberArr = this.globalUtil.randomFromZero(10, 3);
					for (var i = 0; i < numberArr.length; i++) {
						this.liu[numberArr[i]].active = true;
					}
					this.liuarr = this.liu.filter(v => {
						return v.active
					});
					this.acount = this.globalUtil.math(this.liuarr.length, 3);
				} else if (this.zindex == 2 && this.current == 1) {
					let numberArr = this.globalUtil.randomFromZero(26, 1);
					this.groupGentle[numberArr[0]].active = true
					this.groupGentleArr = this.groupGentle.filter(v => {
						return v.active
					})
					this.acount = this.groupGentle[numberArr[0]].pour;
				} else if (this.zindex == 3 && this.current == 1) {
					let numberArr = this.globalUtil.randomFromZero(10, 2);
					this.shi[numberArr[0]].active = true
					this.ge[numberArr[1]].active = true
					this.shiarr = this.shi.filter(v => {
						return v.active
					})
					this.gearr = this.ge.filter(v => {
						return v.active
					})
					this.acount = this.globalUtil.math(this.shiarr.length,
						1) * this.globalUtil.math(
						this.gearr.length, 1)
				}
				this.total = this.acount * 2;
			},
			sure() {
				if (this.total == 0) {
					this.randomSelect();
					return;
				}
				if (this.total == '每位至少选择1个不同的号码') {
					uni.showToast({
						title: '每位至少选择1个不同的号码',
						icon: 'none'
					});
					return;
				}
				let data;
				//随机数id用户传到购物车进行去重处理
				let uid = Math.ceil(Math.random() * 9999999999999999)
				if (this.zindex == 0 && this.current == 0) {
					data = {
						uid: uid,
						mode: 0,
						notes: this.acount,
						total: this.total,
						individual: this.gearr.map(v => {
							return v.num
						}),
						ten: this.shiarr.map(v => {
							return v.num
						}),
						hundred: this.baiarr.map(v => {
							return v.num
						})
					}
				} else if (this.zindex == 1 && this.current == 0) {
					data = {
						uid: uid,
						mode: 3,
						notes: this.acount,
						total: this.total,
						individual: this.directlyElectedGentleArr.map(v => {
							return v.num
						}),
					}
				} else if (this.zindex == 0 && this.current == 1) {
					data = {
						uid: uid,
						mode: 1,
						notes: this.acount,
						total: this.total,
						individual: this.sanarr
					}
				} else if (this.zindex == 1 && this.current == 1) {
					data = {
						uid: uid,
						mode: 2,
						notes: this.acount,
						total: this.total,
						individual: this.liuarr
					}
				} else if (this.zindex == 2 && this.current == 1) {
					data = {
						uid: uid,
						mode: 4,
						notes: this.acount,
						total: this.total,
						individual: this.groupGentleArr.map(v => {
							return v.num
						})
					}
				} else if (this.zindex == 3 && this.current == 1) {
					data = {
						uid: uid,
						mode: 5,
						notes: this.acount,
						total: this.total,
						individual: this.gearr.map(v => {
							return v.num
						}),
						ten: this.shiarr.map(v => {
							return v.num
						}),
					}
				}
				uni.navigateTo({
					url: "/pages/kl8/buyShoppingCar?obj=" + encodeURIComponent(JSON.stringify(data)),
					animationType: 'pop-in',
					animationDuration: 200
				})
			},
		},
	}
</script>
<style scoped>
	/deep/.cmd-nav-bar-right-text {
		font-size: 16px !important;
	}

	/deep/.u-checkbox-group--row {
		justify-content: center;
		align-items: center;
		margin-top: 5px;
		margin-left: 6px;
	}

	.tips {
		padding: 20rpx;
		color: #999;
		font-size: 28rpx;
	}

	.tips span {
		color: #FF3F43;
	}

	.fc {
		margin-bottom: 30rpx;
	}

	.fc ul {
		margin-top: 12rpx;
		padding-left: 36rpx;
	}

	.fc ul p {
		height: 50rpx;
		line-height: 50rpx;
		color: #fff;
		background: #FF3F43;
		width: 100rpx;
		font-size: 30rpx;
		padding-left: 20rpx;
		margin-left: -36rpx;
		border-top-right-radius: 30rpx;
		border-bottom-right-radius: 30rpx;
	}

	.fc ul li {
		display: inline-block;
		width: 74rpx;
		color: #FF3F43;
		background: #fff;
		height: 74rpx;
		text-align: center;
		line-height: 74rpx;
		font-size: 30rpx;
		border: 1px solid #e2e2e2;
		border-radius: 50%;
		margin: 18rpx
	}

	.fc ul li.active {
		background: #FF3F43;
		color: #fff;
	}

	.fc_tab {
		padding: 0 20rpx;
		background: #fff;
		height: 70rpx;
		padding-top: 30px;
	}

	.fc_tab span {
		display: inline-block;
		width: 33%;
		text-align: center;
		line-height: 10rpx;
		height: 45rpx;
		;
		font-size: 34rpx;
		float: left;
	}

	.fc_tab span.active {
		border-bottom: 1px solid #FF3F43;
		color: #FF3F43;
	}

	.fc_index {
		height: 54rpx;
		line-height: 30rpx;
		color: #999;
		font-size: 28rpx;
		background: #fff;
		text-align: center;
		padding-top: 30px;
	}
</style>