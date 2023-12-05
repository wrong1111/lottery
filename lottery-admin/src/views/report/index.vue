<template>

  <div class="app-container">
    <el-tabs type="border-card" @tab-click="tabClick">
      <el-tab-pane label="日报">
        <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch">
          <el-form-item label="统计日期">
            <!-- <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
              range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker> -->
            <el-date-picker v-model="queryParams.start" type="date" placeholder="选择开始日期" value-format="yyyy-MM-dd">
            </el-date-picker>
            <el-date-picker v-model="queryParams.end" type="date" placeholder="选择截止日期" value-format="yyyy-MM-dd">
            </el-date-picker>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <el-divider content-position="center">日营业数据</el-divider>
        <el-row :gutter="24" class="mb8">
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="6">
            <div>
              <StatisticsItem title="订单数" :value="reportData.orderCounts" unit=""></StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.orderMoney - reportData.revokePrice" title="订单金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.receiveCounts" title="收单数" unit=""></StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.receiveMoney" title="收单金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="24" style="margin-top: 40px">
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.changeCounts" title="转单数" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.changeMoney" title="转单金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.changeCounts" title="充值数" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.changeMoney" title="充值金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="24" style="margin-top: 40px">
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.drawCounts" title="提现数" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.drawMoney" title="提现金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>

        </el-row>
        <el-divider content-position="center">总数据</el-divider>
        <el-row :gutter="24">
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.users" title="总会员数" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.allMoney" title="总库存" unit="">
              </StatisticsItem>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane label="月报">

        <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch">
          <el-form-item label="统计月份">
            <el-date-picker v-model="queryParams.start" type="month" placeholder="选择开始月份" value-format="yyyy-MM">
            </el-date-picker>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <el-divider content-position="center">月报数据项</el-divider>
        <el-row :gutter="24" class="mb8">
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="6">
            <div>
              <StatisticsItem title="订单数" :value="reportData.orderCounts" unit=""></StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.orderMoney - reportData.revokePrice" title="订单金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.receiveCounts" title="收单数" unit=""></StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.receiveMoney" title="收单金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="24" style="margin-top: 40px">
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.changeCounts" title="转单数" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.changeMoney" title="转单金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.changeCounts" title="充值数" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.changeMoney" title="充值金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="24" style="margin-top: 40px">
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.drawCounts" title="提现数" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.drawMoney" title="提现金额" unit="">
              </StatisticsItem>
            </div>
          </el-col>
        </el-row>
        <el-divider content-position="center">总数据</el-divider>
        <el-row :gutter="24">
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.users" title="总会员数" unit="">
              </StatisticsItem>
            </div>
          </el-col>
          <el-col :span="6">
            <div>
              <StatisticsItem :value="reportData.allMoney" title="总库存" unit="">
              </StatisticsItem>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
  import {
    dateFormat
  } from '@/api/utils';

  import StatisticsItem from "../dashboard/components/StatisticsItem.vue";
  import {
    reqDayStatisticsData,
    reqMonthStatisticsData
  } from "@/api/statistics";
  export default {
    name: "reportIndex",

    components: {
      StatisticsItem,
    },
    props: {},
    data() {
      return {
        tab: 0,
        dateRange: [],
        // 遮罩层
        loading: false,
        // 查询参数
        queryParams: {
          start: dateFormat(new Date()),
          end: dateFormat(new Date()),
        },
        // 是否显示搜索
        showSearch: true,
        reportData: {
          orderCounts: 0,
          orderMoney: 0.00,
          receiveCounts: 0,
          receiveMoney: 0.00,
          changeCounts: 0,
          changeMoney: 0.00,
          rechargeCounts: 0,
          rechargeMoney: 0.00,
          users: 0,
          allMoney: 0,
          drawCounts: 0,
          drawMoney: 0.00,
          revokePrice: 0.00,
        }
      };
    },
    computed: {},
    watch: {},
    created() {},
    mounted() {
      this.getList();
    },
    methods: {
      tabClick(v, e) {
        //console.log(v.index)
        this.tab = v.index
        this.resetQuery()
      },
      // 获取统计列表
      getList() {
        this.loading = true;
        console.log(' query ', this.queryParams)
        if (this.tab == 0) {
          reqDayStatisticsData(this.queryParams).then((res) => {
            this.loading = false
            if (res.success) {
              this.reportData = res.data
            }
          })
        }else{
            reqMonthStatisticsData(this.queryParams).then((res) => {
            this.loading = false
            if (res.success) {
              this.reportData = res.data
            }
          })
        }
      },
      // 查询数据
      handleQuery() {
        this.getList();
      },
      // 重置数据
      resetQuery() {
        this.resetForm("resetQuery")
        this.queryParams.start = ''
        this.queryParams.end = ''
        this.handleQuery()
      },
    },
  };
</script>

<style>
</style>
