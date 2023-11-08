<template>
	<view class="box">
		
			<nav-bottom :current="3"></nav-bottom>
			<web-view src="https://m.sporttery.cn/mjc/zqsgkj/"></web-view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {}
		},
		methods: {

		},
	}
</script>

<style lang="scss">
	.u-header-back {
		display: none;
	}

	uni-web-view.uni-webview--fullscreen {
		margin-bottom: 146rpx;
	}
</style>
