<template>
  <div class="dashboard-container">
    <el-card>
      <div class="bage-container">
        <el-badge :value="statisticsData.ticketingCount">
          <div style="cursor: pointer">
            <img
              style="width: 50px; height: 50px; display: block"
              src="@/assets/images/waite_ticket.png"
              alt=""
            />
            <span class="remark">待出票</span>
          </div>
        </el-badge>

        <el-badge :value="statisticsData.awardCount">
          <div style="cursor: pointer">
            <img
              style="width: 50px; height: 50px; display: block"
              src="@/assets/images/award.png"
              alt=""
            />
            <span class="remark">待派奖</span>
          </div>
        </el-badge>

        <el-badge :value="statisticsData.withdrawalCount">
          <div style="cursor: pointer">
            <img
              style="width: 50px; height: 50px; display: block"
              src="@/assets/images/withdrawal.png"
              alt=""
            />
            <span class="remark">待提现</span>
          </div>
        </el-badge>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <el-row :gutter="20">
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="彩民数量"
            :value="statisticsData.userCount"
            unit="人"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="今日注册"
            :value="statisticsData.registerCount"
            unit="人"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="今日实名"
            :value="statisticsData.realCount"
            unit="人"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="用户账户总额"
            :value="statisticsData.totalPrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 40px">
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="今日充值"
            :value="statisticsData.rechargePrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="今日提现"
            :value="statisticsData.withdrawalPrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="待派奖金额"
            :value="statisticsData.awardPrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="总已派奖金额"
            :value="statisticsData.alreadyAwardPrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 40px">
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="今日投注"
            :value="statisticsData.bettingPrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="昨日投注"
            :value="statisticsData.yesterdayBettingPrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="昨日派奖"
            :value="statisticsData.yesterdayAwardPrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="昨日盈利"
            :value="statisticsData.yesterdayProfitPrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 40px">
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="总充值"
            :value="statisticsData.totalRechargePrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
        <el-col :span="6" :offset="0">
          <StatisticsItem
            title="总盈利"
            :value="statisticsData.totalProfitPrice"
            :decimals="2"
          ></StatisticsItem>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import StatisticsItem from "./components/StatisticsItem";
import { reqStatisticsData } from "@/api/statistics";

export default {
  name: "Dashboard",
  components: {
    StatisticsItem,
  },
  data() {
    return {
      statisticsData: {},
    };
  },
  computed: {
    ...mapGetters(["name"]),
  },
  mounted() {
    this.getStatisticsData();
  },
  methods: {
    // 获取统计数据
    getStatisticsData() {
      reqStatisticsData().then((response) => {
        this.statisticsData = response;
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 20px;
    .bage-container {
      display: flex;
      justify-content: space-around;
      align-items: center;

      .remark {
        display: block;
        margin-top: 10px;
        font-size: 16px;
      }
    }
  }
}
</style>
