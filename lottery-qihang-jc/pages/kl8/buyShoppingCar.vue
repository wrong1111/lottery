<template>
	<view class="box">

		<nav-bar :title="'快乐8'" :back="true"></nav-bar>
		<div class="shoppingCar_wrap">
			<div class="shoppingCar_content">
				<div class="shoppingCar_add">
					<div class="add_once" @tap="random">+机选1注</div>
					<div class="add_back" @tap="back">+继续选号</div>
					<div class="add_back" @tap="clean">+清空列表</div>
				</div>
				<div class="shoppingCar_added">
					<div class="added_selected" v-for="arr in placeData">
						<div class="selected_left">
							<div class="selected_text">
								<div class="selected_num">
									</span>
									<div style="background-color: #007BED;" class="content" v-for="c in arr.individual">
										<p v-if="c.isGallbladder" style="font-size: 12px;">
											{{'胆'+c.num}}
										</p>
										<p v-else>{{c.num}}</p>
									</div>
								</div>
								<div class="selected_count">
									{{arr.notes}}注 {{arr.total}}元
								</div>
							</div>
						</div>
						<u-icon @tap="del(arr.uid)" name="trash" size="26" color="#909399"></u-icon>
					</div>
					<div class="added_rule">
						<div class="rule_left">
							<span>购买即同意《委托投注协议》</span>
						</div>
					</div>
				</div>
			</div>
			<div class="shoppingCar_footer">
				<view style="display: flex; justify-content: center;align-items: center;">
					投<u-number-box iconStyle="color: #FF3F43" integer inputWidth="70" @change="numberChange">
					</u-number-box>倍
				</view>
				<div class="footer_bottom">
					<div class="bottom_left">
						共 <b>{{acount}}</b>注 <b>{{total}}</b>元
					</div>
					<div class="bottom_right1">
						<button size="mini" type="button" @tap="documentaryBtn">发起跟单</button>
					</div>
					<div class="bottom_right">
						<span @tap="() => confirmIsShow = true">下一步</span>
					</div>
				</div>
			</div>
		</div>
		<u-modal title="投注确认" :show="confirmIsShow" :zoom="false" confirmText="投注" showCancelButton
			confirmColor="#FF3F43" @confirm="betting" @cancel="() => confirmIsShow = false">
			<view class="tip">
				<p>[快乐8]</p>
				<p>第{{issueNo}}期</p>
				<p>共{{acount}}注，您需要支付{{total}}元</p>
			</view>
		</u-modal>

	</view>
</template>

<script>
	import {
		place,
		getIssueNo,
		documentaryDigit,
	} from '@/api/pailie.js'
	export default {
		data() {
			return {
				confirmIsShow: false,
				placeData: [],
				total: 0,
				acount: 0,
				times: 1,
				issueNo: "",
				mode: '',
				lotid: 23
			}
		},
		onLoad(option) {
			if (option.obj != undefined) {
				let obj = JSON.parse(decodeURIComponent(option.obj));
				console.log(' kl8 =>', obj)
				this.calculation(obj);
			}
			//当前期号
			getIssueNo("23").then(res => {
				this.issueNo = res.stageNumber
			})
		},
		methods: {
			//发起跟单
			documentaryBtn() {
				let data = uni.getStorageSync('kl8');
				if (data.length <= 0) {
					uni.showToast({
						title: '至少选择一注',
						icon: 'none'
					});
					return;
				}
				//往数组中添加倍数新字段
				data.forEach(item => {
					this.$set(item, 'times', this.times)
				})
				let placeData = {
					data: data,
					acount: this.acount,
					times: this.times,
					type: this.lotid,
					storage: 'kl8'
				}
				documentaryDigit(placeData)
			},
			//投注
			betting() {
				uni.showLoading();
				let data = uni.getStorageSync('kl8');
				if (data.length <= 0) {
					uni.showToast({
						title: '至少选择一注',
						icon: 'none'
					});
					return;
				}
				if (this.total < 10) {
					uni.showToast({
						title: '下单金额最低10元起投',
						icon: 'none'
					});
					return;
				}
				//往数组中添加倍数新字段
				data.forEach(item => {
					this.$set(item, 'times', this.times)
					delete item['total']
					delete item['uid']
					//处理方案组合
					if (item.schemeDetails == undefined) {
						let map = [];
						map.push({
							"mode": this.mode,
							"stageNumber": this.issueNo,
							"content": item.ten.map(item => item.num).join(','),
							"forecastBonus": null
						})
						//转成字符串json
						map = JSON.stringify(map);
						this.$set(item, 'schemeDetails', map)
					} else {
						this.$set(item, 'schemeDetails', JSON.stringify(item.schemeDetails))
					}
				})
				place(data, "23").then(res => {
					if (res.success) {
						uni.showToast({
							title: '下单成功',
							icon: 'none'
						});
						//标识为已经下单了
						uni.setStorageSync('isPay', true);
						this.clean();
						this.confirmIsShow = false;
						setTimeout(function() {
							uni.hideLoading();
						}, 500);
						uni.navigateTo({
							url: "/pages/order/lotteryOrderDetails?id=" + res.id,
							animationType: 'pop-in',
							animationDuration: 200
						})
					}
				})
			},
			//计算
			calculation(obj) {
				let data = uni.getStorageSync('kl8');
				this.mode = obj.mode
				let isRepeat = false
				if (data.length > 0) {
					data.map(item => {
						//如果有重复进行标记
						if (item.uid == obj.uid) {
							isRepeat = true;
							return;
						}
					})
					//根据标记去重处理
					if (!isRepeat) {
						//如果不是重复的继续叠加
						data.unshift(obj)
						uni.setStorageSync("kl8", data)
					}
					//赋值
					this.placeData = data
					//计算总价和总注数
					data.map(item => {
						this.total += item.total
						this.acount += item.notes
					})
				} else {
					//第一次为空的时候写入到本地缓存
					this.placeData.push(obj)
					uni.setStorageSync("kl8", this.placeData)
					this.total = obj.total;
					this.acount = obj.notes;
				}
			},
			//机选
			random() {
				this.total = 0;
				this.acount = 0;
				let uid = Math.ceil(Math.random() * 9999999999999999)
				let numberArr = this.randomFromZero(80, parseInt(this.mode));
				let data1 = [];
				numberArr.sort((a, b) => a - b)
				//组装数据
				for (var i = 0; i < numberArr.length; i++) {
					data1.push({
						num: numberArr[i],
						active: true,
						isGallbladder: false
					})
				}
				let obj = {
					uid: uid,
					mode: this.mode,
					notes: 1,
					total: 2,
					individual: data1,
					ten: [],
				}
				this.calculation(obj);
			},
			//双色球随机数生成
			randomFromZero(m, n) {
				let exist = []
				for (var i = 0; i < m; i++) {
					exist[i] = 0
				}
				let list = []
				for (var i = 0; i < n;) {
					let index = Math.floor(Math.random() * m);
					if (exist[index] == 0) {
						let num = index + 1;
						if (num < 10) {
							num = '0' + num;
						} else {
							num = '' + num;
						}
						list.push(num)
						i++;
						exist[index] = 1;
					}
				}
				return list
			},
			//删除
			del(uid) {
				let data = uni.getStorageSync('kl8');
				//重新计算总价和总注数
				data.map(item => {
					if (item.uid == uid) {
						this.total -= item.total
						this.acount -= item.notes
					}
				})
				//删除数据
				data = data.filter((item) => {
					return item.uid != uid;
				});
				//重新赋值
				this.placeData = data
				//写入缓存
				uni.setStorageSync("kl8", data)
			},
			numberChange(item) {
				this.total = this.acount * item.value * 2
				this.times = item.value;
			},
			back() {
				uni.navigateTo({
					url: "/pages/kl8/kl8",
					animationType: 'pop-in',
					animationDuration: 200
				})
			},
			//清空
			clean() {
				uni.removeStorageSync("kl8")
				this.placeData = []
				this.total = 0;
				this.acount = 0;
			}
		},
	}
</script>

<style scoped lang="scss">
	.vertical {
		color: #848484;
		padding: 0 10rpx;
	}

	.content {
		width: 30px;
		height: 30px;
		background-color: #FF3F43;
		border-radius: 50%;
		display: inline-block;
		margin-right: 2px;

		p {
			width: 30px;
			height: 30px;
			color: #fff;
			text-align: center;
			line-height: 30px;
			font-size: 12px;
		}
	}

	.tip {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;

		p {
			margin-top: 8rpx;
			color: #606266;
			font-size: 16px;
		}
	}

	.shoppingCar_wrap {
		.shoppingCar_content {
			.shoppingCar_add {
				margin-bottom: 2.26667vmin;
				font-size: 3.73333vmin;
				padding: 0 3.33333vmin;
				display: flex;
				align-items: center;
				justify-content: space-between;

				div {
					width: 48%;
					background-color: white;
					height: 11.46667vmin;
					display: flex;
					justify-content: center;
					align-items: center;
					color: #666;
				}
			}

			.shoppingCar_added {
				box-sizing: border-box;
				margin: 0 26rpx;

				.added_rule {
					box-sizing: border-box;
					font-size: 3.46667vmin;
					height: 12.53333vmin;
					width: 100%;
					background-color: white;
					color: #999;
					display: flex;
					align-items: center;
					justify-content: space-between;
					padding: 0 4.4vmin;
				}

				.added_selected {
					background-color: white;
					padding: 2.66667vmin;
					box-sizing: border-box;
					border-bottom: 1px solid #e6e6e6;
					display: flex;
					align-items: center;
					justify-content: space-between;

					.selected_left {
						display: flex;
						align-items: center;
						justify-content: center;

						.selected_text {

							.selected_num {
								color: #FF5562;
								font-size: 5.33333vmin;
								margin-bottom: 1.8vmin;
							}

							.selected_count {
								font-size: 3.46667vmin;
								color: #999;
							}
						}
					}
				}
			}
		}

		.shoppingCar_footer {
			width: 100%;
			position: fixed;
			bottom: 0;
			left: 0;
			color: #8f9090;
			background-color: white;

			.footer_bottom {
				box-sizing: border-box;
				width: 100%;
				display: flex;
				align-items: center;
				justify-content: space-between;
				height: 13.33333vmin;
				padding-left: 2.66667vmin;

				.bottom_left {
					color: #333333;
					font-size: 4vmin;

					b {
						color: #FF5562;
					}
				}

				.bottom_right1 {
					/*border: none;*/
					text-align: center;
					color: #FFFFFF;
					font-size: 4.8vmin;
					width: 28.53333vmin;
					border-radius: 4px;
					height: 100%;
					display: flex;
					align-items: center;
					justify-content: center;
				}

				.bottom_right {
					/*border: none;*/
					text-align: center;
					background: #FF3F43;
					color: #FFFFFF;
					font-size: 4.8vmin;
					width: 28.53333vmin;
					border-radius: 4px;
					height: 100%;
					display: flex;
					align-items: center;
					justify-content: center;
				}
			}
		}
	}
</style>