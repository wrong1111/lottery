<template>
  <div>
    <el-dialog v-bind="$attrs" v-on="$listeners" width="680px" @open="onOpen" @close="onClose">
      <el-form :model="form" ref="addShopForm" size="medium" label-width="100px">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="店名" prop="shopName">
              <el-input v-model="form.shopName" placeholder="请输入下游 店名">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="shopConcatPhone">
              <el-input v-model="form.shopConcatPhone" placeholder="请输入联系方式">
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="接口账号" prop="transferKey">
              <el-input v-model="form.transferKey" placeholder="请输入接口账号 5位字符">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="shopConcatName">
              <el-input v-model="form.shopConcatName" placeholder="联系人">
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div slot="footer">
        <el-button @click="close">取 消</el-button>
        <el-button type="primary" @click="handleConfirm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import {
    Message
  } from "element-ui";

  export default {
    name: "AddPlatTransfer",
    components: {},
    props: {
      lots: {
        type: Array
      },
      form: {
        id:{
          type:Object
        },
        shopName: {
          type: Object
        },
        shopConcatPhone: {
          type: Object
        },
        shopConcatName: {
          type: Object
        },
        transferKey: {
          type: Object
        },
        transferSecurty: {
          type: Object
        },
        interfaceState: {
          type: Number
        }
      },
    },
    data() {
      return {

      };
    },
    computed: {},
    watch: {},
    created() {},
    mounted() {

    },
    methods: {
      onOpen() {},
      onClose() {},
      close() {
        this.resetForm("addShopForm");
        this.$emit("update:visible", false);
      },
      handleConfirm() {
        this.$refs.addShopForm.validate((valid) => {
          if (!valid) return;
          this.$emit("confirm", {
            ...this.form
          });
          this.close();
        });
      },
    },
  };
</script>

<style scoped lang="scss"></style>
