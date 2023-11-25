<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" size="mini">
      <el-form-item label="彩种状态" prop="name">
        <el-select v-model="queryParams.state">
          <el-option v-for="(item,index) in states" :key="item.id" :value="item.value" :label="item.label"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8" style="width: 50%;align-items: center;display: flex;flex-wrap: wrap;"  v-if="isOpen">
      <el-col :span="1.5" class="shop">
        店铺名: {{transfer.shopName}}
      </el-col>
      <el-col :span="1.5" class="shop">
        联系方式: {{transfer.shopConcatPhone}}
      </el-col>
      <el-col :span="1.5" class="shop">
        联系人: {{transfer.shopConcatName}}
      </el-col>
    </el-row>
    <el-row :gutter="10" class="mb8" style="width: 50%;align-items: center;display: flex;flex-wrap: wrap;"  v-if="isOpen">
      <el-col :span="1.5" class="shop">
        key: {{transfer.transferKey}}
      </el-col>
      <el-col :span="1.5" class="shop">
        秘钥: {{transfer.transferSecurty}}
      </el-col>
      <el-col :span="1.5" class="shop">
        接口地址: {{transfer.transferInterface}}
      </el-col>
    </el-row>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" icon="el-icon-circle-plus-outline" size="mini" @click="addNewShop"
          plain>添加收单彩种</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="voList" border v-if="isOpen">
      <el-table-column label="ID" align="center" prop="lotteryType" />
      <el-table-column label="彩种名称" align="center" prop="lotteryName" />
      <el-table-column label="LOGO" align="center">
        <template slot-scope="scope">
          <el-image style="width: 60px; height: 60px" :src="scope.row.icon" fit="cover"></el-image>
        </template>
      </el-table-column>

      <el-table-column label="开通收单" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.states == "0" ? "开通" : "停止" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="截止前(秒)" align="center" prop="transferBeforeTime" />
      <el-table-column label="返点(%)" align="center" prop="commiss" />
      <el-table-column label="创建时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="danger" plain @click="editStateRow(scope.row,0)"
            v-if="scope.row.states==1">开通</el-button>
          <el-button size="mini" type="primary" plain @click="editStateRow(scope.row,1)"
            v-if="scope.row.states==0">停止</el-button>
          <el-button size="mini" type="success" plain @click="editTransfer(scope.row)">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-table v-else>
      <el-table-column> 暂未开通</el-table-column>
    </el-table>
    <!-- <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
      @pagination="getList" /> -->

    <AddLotteryTransfer title="开通彩种收单" :visible.sync="dialogAddVisible" @confirm="addShopReq" :lots="lots" :form="form">
    </AddLotteryTransfer>

  </div>
</template>

<script>
  import {
    shopTransferList,
    editShopTransfer,
    addLotteryTransfer,
    editState,
    editCommiss,
    noOpenLottery
  } from "@/api/transfer/in";
  import {
    deepClone
  } from "@/utils";
  import {
    Message,
    MessageBox
  } from "element-ui";
  import AddLotteryTransfer from "./components/AddLotteryTransfer.vue";
  import ShopRechargeDialog from "./components/ShopRechargeDialog.vue";
  import ShopPayConfigDialog from "./components/ShopPayConfigDialog.vue";

  export default {
    name: "transfein",
    components: {
      AddLotteryTransfer,
      ShopRechargeDialog,
      ShopPayConfigDialog,
    },
    props: {},
    data() {
      return {
        form: {
          lotteryType: '',
          commiss: 5,
          state: 0,
          //目前只用于平台一家，写死
          shopId: 1,
          beforeTime: 600,
        },
        states: [{
          'value': '',
          'label': '所有'
        }, {
          'value': 0,
          'label': '正常'
        }, {
          'value': 1,
          'label': '停止'
        }],
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        isOpen: false, //未设置
        // 彩种列表
        voList: [],
        lots: [],
        transfer: undefined, //设置信息
        // 遮罩层
        loading: true,
        // 用户查询
        queryParams: {
          pageNo: 1,
          pageSize: 10,
          state: undefined,
        },
        // 添加用户弹窗
        dialogAddVisible: false,
        // 支付配置
        dialogConfigVisible: false,
        // 充值弹窗
        dialogRechargeVisible: false,
        // 当前需要修改的用户
        updateShop: {},
      };
    },
    computed: {},
    watch: {},
    created() {},
    mounted() {
      this.getList()
      this.loadLotid()
    },
    methods: {
      loadLotid() {
        noOpenLottery().then((res) => {
          this.lots = res.voList
        })
      },
      //修改
      editTransfer(row) {
        this.form = {
            lotteryType: row.lotteryType,
            commiss: row.commiss,
            state: row.states,
            //目前只用于平台一家，写死
            shopId: 1,
            beforeTime: row.transferBeforeTime,
          },
          this.dialogAddVisible = true;
      },
      editStateRow(row, state) {
        editState({
          id: row.id,
          states: state
        }).then((res) => {
          this.getList()
        })
      },
      // 获取用户列表
      getList() {
        this.loading = true;
        shopTransferList().then((response) => {
          this.loading = false;
          this.isOpen = response.data.transfer == 1 ? true : false
          if (this.isOpen) {
            this.voList = response.data.lotterys
            this.transfer = response.data.shop
          }

        });
      },
      // 条件查询
      handleQuery() {
        this.queryParams.pageNo = 1;
        this.getList();
      },
      // 重置查询条件
      resetQuery() {
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // ---------------- 用户相关操作 -----------------------
      // 新增用户
      addNewShop() {
        this.dialogAddVisible = true;
      },

      // 发送新增 收单彩种请求
      addShopReq(data) {
        addLotteryTransfer(data).then((response) => {
          if (!response.errorCode) {
            this.getList()
            this.loadLotid()
          }
        });
      },

    },
  };
</script>

<style scoped lang="scss">
  .shop {
    width: 30%
  }
</style>
