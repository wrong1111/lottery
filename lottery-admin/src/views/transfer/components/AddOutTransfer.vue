<template>
  <div>
    <el-dialog v-bind="$attrs" v-on="$listeners" width="680px" @open="onOpen" @close="onClose">
      <el-form :model="form" ref="addShopForm"   size="medium" label-width="100px">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="秘钥" prop="lotteryType">
                <el-select v-model="form.lotteryType">
                  <el-option v-for="(item,index) in lots" :value="item.lotid" :label="item.name + (item.selected?'(已开通)':'') " :key="index"></el-option>
                </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="返点" prop="commiss">
                <el-input  v-model="form.commiss" placeholder="请输入返点 5 表示 5%">
                  <template slot="append">%</template>
                </el-input>
            </el-form-item>
          </el-col>

        </el-row>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="收单提前截止时间(秒)" prop="beforeTime">
              <el-input  v-model="form.beforeTime" placeholder="请输入 收单提前截止时间">
                  <template slot="append">秒</template>
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
    name: "AddOutTransfer",
    components: {},
    props: {
      lots:{
        type:Array
      },
      form: {
        lotteryType:Number,
        commiss:Number,
        state:Number,
        //目前只用于平台一家，写死
        shopId:Number,
        beforeTime:Number,
      },
    },
    data() {
      return {

      };
    },
    computed: {},
    watch: {},
    created() {
    },
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
