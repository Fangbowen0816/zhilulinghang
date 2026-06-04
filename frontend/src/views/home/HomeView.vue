<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div class="stat-title">用户数量</div>
          <div class="stat-number">128</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card>
          <div class="stat-title">项目数量</div>
          <div class="stat-number">36</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card>
          <div class="stat-title">今日访问</div>
          <div class="stat-number">892</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card>
          <div class="stat-title">AI 调用次数</div>
          <div class="stat-number">57</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card">
      <h2>欢迎使用智路领航平台</h2>
      <p>这里是系统首页，后续可以展示项目概览、用户统计、AI 使用情况等信息。</p>
    </el-card>
  </div>
</template>

<style scoped>
.home {
  padding: 20px;
}

.stat-title {
  color: #666;
  font-size: 14px;
}

.stat-number {
  margin-top: 12px;
  font-size: 28px;
  font-weight: bold;
}

.welcome-card {
  margin-top: 20px;
}
</style>