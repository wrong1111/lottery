<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" icon="el-icon-circle-plus-outline" size="mini" @click="addNewShop"
          plain>添加收单商家</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="voList" border>
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="下游 店名" align="center" prop="shopName" />
      <el-table-column label="店余额" align="right" prop="shopName">
        <template slot-scope="scope">
          <span @click="optmoney(scope.row)">{{ (scope.row.golds+scope.row.money).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="开通收单" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.interfaceState == "0" ? "开通" : "停止" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="下游联系人" align="center" prop="shopConcatName" />
      <el-table-column label="下游联系方式" align="center" prop="shopConcatPhone" />
      <el-table-column label="一键获取" align="center" prop="gateinfo">
      </el-table-column>
      <el-table-column label="接口账号" align="center" prop="transferKey">
      </el-table-column>

      <el-table-column label="接口秘钥" align="center" prop="transferSecurty">
        <template slot-scope="scope">
          <span> {{scope.row.transferSecurty}}</span>
          </br>
          <el-button size="mini" type="primary" @click="resetSecurity(scope.row)">重置</el-button>
        </template>
      </el-table-column>
      <el-table-column label="接口地址" align="center" prop="transferInterface" />
      <el-table-column label="创建时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="danger" plain @click="editStateRow(scope.row,0)"
            v-if="scope.row.interfaceState==1">开通</el-button>
          <el-button size="mini" type="primary" plain @click="editStateRow(scope.row,1)"
            v-if="scope.row.interfaceState==0">停止</el-button>
          <el-button size="mini" type="success" plain @click="editTransfer(scope.row)">修改</el-button>
          <el-button size="mini" type="primary" plain @click="optmoney(scope.row)">充值</el-button>
        </template>
      </el-table-column>
    </el-table>

    <AddPlatTransfer title="开通收单账号" :visible.sync="dialogAddVisible" @confirm="addShopReq" :lots="lots" :form="form">
    </AddPlatTransfer>
    <verify-pwd-dialog :visible.sync="dialogVerifyPwdVisible" title="交易密码确认" @confirm="verifyPwd"></verify-pwd-dialog>
    <RechargeDialog :visible.sync="dialogRechargeVisible" title="充值设置" :nickname="nickname" @confirm="rechargeReq">
    </RechargeDialog>
  </div>
</template>

<script>
  import {
    verifyUserPwd,
    rechargeUser,
  } from "@/api/client";
  import {
    platlist,
    platSecurityReset,
    editPlatState,
    editShopTransfer
  } from "@/api/transfer/in";
  import {
    deepClone
  } from "@/utils";
  import {
    Message,
    MessageBox
  } from "element-ui";
  import AddPlatTransfer from "./components/AddPlatTransfer.vue";
  import VerifyPwdDialog from "../client/components/VerifyPwdDialog.vue";
  import RechargeDialog from "../client/components/RechargeDialog.vue";
  export default {
    name: "transfein",
    components: {
      AddPlatTransfer,
      VerifyPwdDialog,
      RechargeDialog
    },
    props: {},
    data() {
      return {
        nickname: '',
        uid: '',
        dialogRechargeVisible: false,
        dialogVerifyPwdVisible: false,
        showSearch: false,
        form: {
          id: '',
          shopName: '',
          shopConcatPhone: '',
          shopConcatName: '',
          transferKey: '',
          transferSecurty: '',
          interfaceState: 0

        },
        // 总条数
        total: 0,
        isOpen: false, //未设置
        // 商家列表
        voList: [],
        lots: [],
        transfer: undefined, //设置信息
        // 遮罩层
        loading: true,
        // 添加用户弹窗
        dialogAddVisible: false,
        // 当前需要修改的用户
        updateShop: {},
      };
    },
    computed: {},
    watch: {},
    created() {},
    mounted() {
      this.getList()
    },
    methods: {
      // 充值
      optmoney(row) {
        this.nickname = row.shopName
        this.uid = row.uid
        this.dialogVerifyPwdVisible = true
      },

      // 交易密码确认
      verifyPwd(form) {
        let {
          payPwd
        } = form;
        let queryParams = {
          username: this.nickname,
          payPwd: payPwd,
        };
        verifyUserPwd(queryParams).then((response) => {
          if (!response.errorCode) {
            this.dialogRechargeVisible = true;
          }
        });
      },

      // 充值请求
      rechargeReq(form) {
        rechargeUser(this.uid, form).then((response) => {
          if (!response.errorCode) {
            this.getList();
          }
        });
      },
      //修改
      editTransfer(row) {
        this.form = {
            id: row.id,
            shopName: row.shopName,
            shopConcatPhone: row.shopConcatPhone,
            shopConcatName: row.shopConcatName,
            transferKey: row.transferKey,
            transferSecurty: row.transferSecurty,
            interfaceState: row.interfaceState
          },
          this.dialogAddVisible = true;
      },
      resetSecurity(row) {
        this.$confirm('此操作将重置收单秘钥, 会导致下游收单接口出现异常,您是否已经清楚其中的风险，是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          platSecurityReset(row.id, '').then((res) => {
            this.getList()
          })

        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消重置'
          });
        });

      },
      editStateRow(row, state) {
        console.log(row)
        editPlatState(row.id, state).then((res) => {
          this.getList()
        })
      },
      // 获取用户列表
      getList() {
        this.loading = true;
        platlist().then((response) => {
          this.loading = false
          this.voList = response.data
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
        editShopTransfer(data).then((res) => {
          if (!res.success) {
            this.$message({
              type: 'info',
              message: res.errorMsg
            });
            return
          }
          this.getList()
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
