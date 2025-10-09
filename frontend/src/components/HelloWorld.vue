<script setup>
import { useI18n } from 'vue-i18n';

const {t} = useI18n();
</script>

<template>
<h1 class="text-blue-500">{{ t('Hello_World') }}</h1>
</template>

<style scoped>

</style>
