<template>
  <v-chart :option="option" autoresize style="width: 100%; height: 260px" />
</template>

<script setup>
import { computed } from 'vue'
import VChart from 'vue-echarts'
import 'echarts'

const props = defineProps({
  data: { type: Array, default: () => [] },   // [{name, value}]
  title: { type: String, default: '' }
})

const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']

const option = computed(() => ({
  title: props.title ? { text: props.title, left: 'center', textStyle: { fontSize: 14, color: '#303133' } } : undefined,
  tooltip: { trigger: 'item', formatter: '{b}: {c}' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    center: ['50%', '55%'],
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    label: { color: '#606266', fontSize: 12 },
    data: props.data.map((d, i) => ({ ...d, itemStyle: { color: colors[i % colors.length] } }))
  }]
}))
</script>
