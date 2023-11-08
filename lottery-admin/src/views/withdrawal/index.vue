<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      :inline="true"
      v-show="showSearch"
      size="mini"
    >
      <el-form-item label="提现类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="提现类型" clearable>
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="打款状态" prop="state">
        <el-select v-model="queryParams.state" placeholder="打款状态" clearable>
          <el-option
            v-for="item in stateOptions"
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
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="用户昵称：">
                    <span>{{ scope.row.nickname }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号：">
                    <span>{{ scope.row.phone }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="实名：">
                    <span>{{ scope.row.name }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份证号：">
                    <span>{{ scope.row.idCard }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="提现金额：">
                    <span>{{ scope.row.amount }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="提现方式：">
                    <span>{{ scope.row.type === "0" ? "支付宝" : "银行卡" }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="提现状态：">
                    <span>{{ getWithdrawalState(scope.row) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12" v-if="scope.row.type === '0'">
                  <el-form-item label="支付宝账号：">
                    <span>{{ scope.row.zfbNumber }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12" v-if="scope.row.type === '1'">
                  <el-form-item label="银行卡账号：">
                    <span>{{ scope.row.bankNumber }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12" v-if="scope.row.type === '1'">
                  <el-form-item label="银行：">
                    <span>{{ scope.row.bankName }}</span>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
        </template>
      </el-table-column>
      <el-table-column label="用户ID" align="center" prop="id" width="100" />
      <el-table-column
        label="昵称"
        align="center"
        prop="nickname"
        width="100"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="提现方式" align="center" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.type === "0" ? "支付宝" : "银行卡" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提现状态" align="center" width="100">
        <template slot-scope="scope">
          <span>{{ getWithdrawalState(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="提现金额"
        align="center"
        prop="amount"
        width="100"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="创建时间" align="center">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="修改时间" align="center">
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
import { getWithdrawalList } from "@/api/withdrawal";

export default {
  name: "Log",
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
        phone: undefined,
        state: undefined, // "0" 待审核 "1" 已打款 "2" 拒绝
        type: undefined, // "0" 支付宝 "1" 银行卡
        pageNo: 1,
        pageSize: 10,
      },
      // 是否显示搜索
      showSearch: true,
      // 是否全部展开
      isExpand: true,
      // 重新渲染表格状态
      refreshTable: true,
      // 提现类型
      typeOptions: [
        {
          label: "支付宝",
          value: "0",
        },
        {
          label: "银行卡",
          value: "1",
        },
      ],
      // 打款状态
      stateOptions: [
        {
          label: "待审核",
          value: "0",
        },
        {
          label: "已打款",
          value: "1",
        },
        {
          label: "拒绝",
          value: "2",
        },
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
    // 提现状态
    getWithdrawalState(row) {
      // "0" 待审核 "1" 已打款 "2" 拒绝
      switch (row.state) {
        case "0":
          return "待审核";
        case "1":
          return "已打款";
        case "2":
          return "拒绝";
        default:
          return "";
      }
    },
    // 获取统计列表
    getList() {
      this.loading = true;
      getWithdrawalList(this.queryParams).then((response) => {
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
