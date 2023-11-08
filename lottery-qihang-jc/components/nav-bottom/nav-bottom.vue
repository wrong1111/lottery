<template>
	<!-- 底部导航栏 -->
	<view>
		<view>
			<cmd-bottom-nav background-color="#FF3F43"
				font-color="#fff" active-font-color="#fff" :current="current" :list="tabBarList" text-auto
				@click="router">
			</cmd-bottom-nav>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				tabBarList: [{
					"pagePath": "pages/home/home",
					"text": "首页",
					"src": "../../static/tabbar/home.png",
					"srcSelect": "../../static/tabbar/home-active.png"
				},{
					"pagePath": "pages/kaijiang/kaijiang",
					"text": "开奖",
					"src": "../../static/tabbar/kaijiang.png",
					"srcSelect": "../../static/tabbar/kaijiang-active.png"
				}
				, {
					"pagePath": "pages/documentary/documentary",
					"text": "跟单",
					"src": "../../static/tabbar/documentary.png",
					"srcSelect": "../../static/tabbar/documentary-active.png"
				}, {
					"pagePath": "pages/score/score",
					"text": "赛果",
					"src": "../../static/tabbar/score.png",
					"srcSelect": "../../static/tabbar/score-active.png"
				}, {
					"pagePath": "pages/personal/personal",
					"text": "我的",
					"src": "../../static/tabbar/user.png",
					"srcSelect": "../../static/tabbar/user-active.png"
				}],
			}
		},
		props: {
			current: {
				type: Number,
				default: 0
			}
		},
		methods: {
			router(e) {
				uni.navigateTo({
					url: e.item.pagePath
				})
			}
		}
	}
</script>

<style>
</style>
