<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      :inline="true"
      v-show="showSearch"
      size="mini"
    >
      <el-form-item label="订单号" prop="orderId">
        <el-input
          v-model="queryParams.orderId"
          placeholder="请输入订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单状态" prop="state">
        <el-select v-model="queryParams.state" placeholder="订单状态" clearable>
          <el-option
            v-for="item in stateOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="流水类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="流水类型" clearable>
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="info"
          icon="el-icon-sort"
          size="mini"
          plain
          @click="toggleExpandAll"
        >
          展开/折叠
        </el-button>
      </el-col>

      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table
      ref="table"
      v-if="refreshTable"
      v-loading="loading"
      :data="voList"
      border
      stripe
      row-key="id"
      :default-expand-all="isExpand"
    >
      <el-table-column type="expand">
        <template slot-scope="scope">
          <el-card>
            <el-form label-position="left" inline class="demo-table-expand">
              <el-row :gutter="5">
                <el-col :span="12">
                  <el-form-item label="订单号：">
                    <span>{{ scope.row.orderId }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="用户名：">
                    <span>{{ scope.row.nickname }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="订单类型：">
                    <span>{{ getOrderType(scope.row) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="支付方式：">
                    <span>{{ getOrderPayStyle(scope.row) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="支付状态：">
                    <span>{{ getOrderPayState(scope.row) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="金额：">
                    <span>{{ scope.row.price }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="创建时间：">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="修改时间：">
                    <span>{{ parseTime(scope.row.updateTime) }}</span>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
        </template>
      </el-table-column>
      <el-table-column
        label="订单号"
        align="center"
        prop="orderId"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="用户名"
        align="center"
        prop="nickname"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="订单类型"
        align="center"
        width="180"
        :show-overflow-tooltip="true"
      >
        <template slot-scope="scope">
          <span style="white-space: nowrap">{{ getOrderType(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" align="center" width="100">
        <template slot-scope="scope">
          <span>{{ getOrderPayStyle(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" width="100">
        <template slot-scope="scope">
          <span>{{ getOrderPayState(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="金额"
        align="center"
        prop="price"
        width="100"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="创建时间" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="修改时间" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
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
import { getPayOrderList } from "@/api/order";

export default {
  name: "OrderPay",
  components: {},
  props: {},
  data() {
    return {
      // 遮罩层
      loading: false,
      // 总数据条数
      total: 0,
      // 列表数据
      voList: [],
      // 查询参数
      queryParams: {
        orderId: undefined,
        state: undefined,
        type: undefined,
        phone: undefined,
        pageNo: 1,
        pageSize: 10,
      },
      // 是否显示搜索
      showSearch: true,
      // 是否全部展开
      isExpand: false,
      // 重新渲染表格状态
      refreshTable: true,
      // 订单状态
      stateOptions: [
        {
          label: "待支付",
          value: "0",
        },
        {
          label: "已支付",
          value: "1",
        },
      ],
      // 流水类型
      typeOptions: [
        {
          label: "账户充值",
          value: "0",
        },
        {
          label: "下单竞彩足球（扣除）",
          value: "1",
        },
        {
          label: "下单竞彩篮球（扣除）",
          value: "2",
        },
        {
          label: "下单北京单场（扣除）",
          value: "3",
        },
        {
          label: "下单排列3（扣除）",
          value: "4",
        },
        {
          label: "提现打款",
          value: "5",
        },
        {
          label: "竞彩足球（派奖）",
          value: "6",
        },
        {
          label: "竞彩篮球（派奖）",
          value: "7",
        },
        {
          label: "北京单场（派奖）",
          value: "8",
        },
        {
          label: "排列3（派奖）",
          value: "9",
        },
        {
          label: "竞彩足球（退票）",
          value: "10",
        },
        {
          label: "竞彩篮球 (退票）",
          value: "11",
        },
        {
          label: "北京单场（退票）",
          value: "12",
        },
        {
          label: "排列3（退票）",
          value: "13",
        },
        {
          label: "发单返佣金",
          value: "14",
        },
        {
          label: "排列五",
          value: "15",
        },
        {
          label: "排列五（派奖）",
          value: "16",
        },
        {
          label: "排列五（退票）",
          value: "17",
        },
        {
          label: "下单七星彩",
          value: "18",
        },
        {
          label: "七星彩（派奖）",
          value: "19",
        },
        {
          label: "七星彩（退票）",
          value: "20",
        },
        {
          label: "下单足球14场",
          value: "21",
        },
        {
          label: "足球14场（派奖）",
          value: "22",
        },
        {
          label: "足球14场（退票）",
          value: "23",
        },
        {
          label: "下单任选九",
          value: "24",
        },
        {
          label: "任选九（派奖）",
          value: "25",
        },
        {
          label: "任选九（退票）",
          value: "26",
        },
        {
          label: "下单大乐透",
          value: "27",
        },
        {
          label: "大乐透（派奖）",
          value: "28",
        },
        {
          label: "大乐透（退票）",
          value: "29",
        },
        {
          label: "下单福彩3D",
          value: "30",
        },
        {
          label: "福彩3D（派奖）",
          value: "31",
        },
        {
          label: "福彩3D（退票）",
          value: "32",
        },
        {
          label: "下单双色球",
          value: "33",
        },
        {
          label: "双色球（派奖）",
          value: "34",
        },
        {
          label: "双色球（退票）",
          value: "35",
        },
        {
          label: "下单快乐8",
          value: "36",
        },
        {
          label: "快乐8（派奖）",
          value: "37",
        },
        {
          label: "快乐8（退票）",
          value: "38",
        },
        {
          label: "下单七乐彩",
          value: "39",
        },
        {
          label: "七乐彩（派奖）",
          value: "40",
        },
        {
          label: "七乐彩（退票）",
          value: "41",
        }
      ],
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
      getPayOrderList(this.queryParams).then((response) => {
        this.loading = false;
        if (!response.errorCode) {
          this.total = response.total;
          this.voList = response.voList;
        }
      });
    },
    // 查询数据
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    // 重置数据
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },

    // 展开/收缩
    toggleExpandAll() {
      this.refreshTable = false;
      this.isExpand = !this.isExpand;
      this.$nextTick(() => {
        this.refreshTable = true;
      });
    },
    // 订单支付状态
    getOrderPayState(row) {
      // "0" 待支付 "1" 已支付 "2" 已打款
      switch (row.state) {
        case "0":
          return "待支付";
        case "1":
          return "已支付";
        default:
          return "已打款";
      }
    },
    // 订单支付方式
    getOrderPayStyle(row) {
      // "0" 支付宝 "1" 微信 "2" APP支付
      switch (row.payType) {
        case "0":
          return "支付宝";
        case "1":
          return "微信";
        case "2":
          return "APP支付";
        default:
          return "";
      }
    },
    // 获取订单类型
    getOrderType(row) {
      let result = this.typeOptions.filter((item) => {
        return item.value === row.type;
      });
      if (result.length === 0) {
        return "";
      }
      return result[0].label;
    },
  },
};
</script>

<style scoped lang="scss">
::v-deep .el-table__expanded-cell[class*="cell"] {
  padding: 10px;
}
.demo-table-expand {
  font-size: 0;
  ::v-deep .el-form-item__label {
    padding: 0px;
  }
  .label {
    width: 90px;
    color: #99a9bf;
  }
  .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 50%;
  }
}
</style>
