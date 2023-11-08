<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      :inline="true"
      v-show="showSearch"
      size="mini"
    >
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="voList"
      border
      stripe
      :summary-method="getSummaries"
      :show-summary="showSummary"
    >
      <el-table-column label="昵称" align="center" prop="nickname" width="100" fixed />
      <el-table-column
        label="手机号"
        align="center"
        prop="phone"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column label="代理" align="center" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.isAgent === "1" ? "是" : "否" }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="下注金额"
        align="center"
        prop="bettingPrice"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column
        label="充值金额"
        align="center"
        prop="rechargePrice"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column
        label="提现金额"
        align="center"
        prop="withdrawalPrice"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column
        label="彩金金额"
        align="center"
        prop="gold"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column
        label="奖金余额"
        align="center"
        prop="price"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column label="上级" align="center" prop="parentName" />
      <el-table-column label="操作" align="center" width="100">
        <template slot-scope="scope">
          <el-button size="mini" type="warning" @click="queryChild(scope.row)"
            >查看下级</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNo"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { userStatistics } from "@/api/client";
import { addDateRange } from "@/utils/ruoyi";

export default {
  name: "ClientStatistics",
  components: {},
  props: {},
  data() {
    return {
      // 遮罩层
      loading: false,
      // 总计显示
      showSummary: false,
      // 总数据条数
      total: 0,
      // 列表数据
      voList: [],
      // 提现金额
      totalWithdrawalPrice: 0,
      // 奖金余额
      totalPrice: 0,
      // 奖金余额
      totalGold: 0,
      // 充值金额
      totalRechargePrice: 0,
      // 下注金额
      totalBettingPrice: 0,
      // 查询参数
      queryParams: {
        startTime: undefined,
        endTime: undefined,
        pageNo: 1,
        pageSize: 10,
      },
      // 是否显示搜索
      showSearch: true,
      // 日期范围
      dateRange: [],
    };
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {
    this.getList();
  },
  methods: {
    // 获取统计列表
    getList() {
      this.loading = true;
      userStatistics(addDateRange(this.queryParams, this.dateRange)).then((response) => {
        this.loading = false;

        if (!response.errorCode) {
          this.voList = response.voList;
          this.total = response.total;
          this.totalWithdrawalPrice = response.totalWithdrawalPrice;
          this.totalPrice = response.totalPrice;
          this.totalGold = response.totalGold;
          this.totalRechargePrice = response.totalRechargePrice;
          this.totalBettingPrice = response.totalBettingPrice;

          this.$nextTick().then(() => {
            this.showSummary = true;
          });
        }
      });
    },
    // 总计数据
    getSummaries(param) {
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = "总计";
          return;
        }

        let prop = column.property;
        if (prop === "bettingPrice") {
          // 下注金额
          sums[index] = this.totalBettingPrice;
        } else if (prop === "rechargePrice") {
          // 充值金额
          sums[index] = this.totalRechargePrice;
        } else if (prop === "gold") {
          // 彩金金额
          sums[index] = this.totalGold;
        } else if (prop === "price") {
          // 奖金余额
          sums[index] = this.totalPrice;
        } else if (prop === "withdrawalPrice") {
          // 提现金额
          sums[index] = this.totalWithdrawalPrice;
        } else {
          sums[index] = "";
        }
      });
      return sums;
    },
    // 查询数据
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    // 重置数据
    resetQuery() {
      this.resetForm("resetQuery");
      this.dateRange = [];
      this.handleQuery();
    },
    // 查看下级
    queryChild(row) {},
  },
};
</script>

<style scoped lang="scss"></style>
