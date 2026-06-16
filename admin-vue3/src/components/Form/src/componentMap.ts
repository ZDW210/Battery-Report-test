import { h } from 'vue'
import type { Component } from 'vue'
import {
  ElCascader,
  ElCheckboxGroup,
  ElColorPicker,
  ElDatePicker,
  ElInput,
  ElInputNumber,
  ElRadioGroup,
  ElRate,
  ElSelect,
  ElSelectV2,
  ElTreeSelect,
  ElSlider,
  ElSwitch,
  ElTimePicker,
  ElTimeSelect,
  ElTransfer,
  ElAutocomplete,
  ElDivider
} from 'element-plus'
import { InputPassword } from '@/components/InputPassword'
import { UploadImg, UploadImgs, UploadFile } from '@/components/UploadFile'
import { ComponentName } from '@/types/components'

const EditorPlaceholder = {
  name: 'EditorPlaceholder',
  render: () => h(ElInput, { type: 'textarea', rows: 6 })
}

const componentMap: Recordable<Component, ComponentName> = {
  Radio: ElRadioGroup,
  Checkbox: ElCheckboxGroup,
  CheckboxButton: ElCheckboxGroup,
  Input: ElInput,
  Autocomplete: ElAutocomplete,
  InputNumber: ElInputNumber,
  Select: ElSelect,
  Cascader: ElCascader,
  Switch: ElSwitch,
  Slider: ElSlider,
  TimePicker: ElTimePicker,
  DatePicker: ElDatePicker,
  Rate: ElRate,
  ColorPicker: ElColorPicker,
  Transfer: ElTransfer,
  Divider: ElDivider,
  TimeSelect: ElTimeSelect,
  SelectV2: ElSelectV2,
  TreeSelect: ElTreeSelect,
  RadioButton: ElRadioGroup,
  InputPassword: InputPassword,
  Editor: EditorPlaceholder,
  UploadImg: UploadImg,
  UploadImgs: UploadImgs,
  UploadFile: UploadFile
}

export { componentMap }
