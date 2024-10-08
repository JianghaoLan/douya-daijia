"use strict";
const propsdetail = {
  _class: {
    type: String,
    default: ""
  },
  _style: {
    type: String,
    default: ""
  },
  width: {
    type: Number,
    default: 0
  },
  height: {
    type: Number,
    default: 600
  },
  /** 提示语，组件内部默认值为 ['下拉刷新', '松手刷新', '正在刷新', '刷新完成'] */
  loadingTexts: {
    type: Array,
    default: () => ["下拉刷新", "松手刷新", "正在刷新", "刷新完成"]
  },
  /** 最大下拉高度，如果值为数字则单位是：'px' */
  maxBarHeight: {
    type: Number,
    default: 120
  },
  /** 刷新超时时间 */
  refreshTimeout: {
    type: Number,
    default: 1e3 * 5
  },
  /** 组件状态，值为 `true` 表示下拉状态，值为 `false` 表示收起状态 */
  modelValue: {
    type: [Boolean],
    default: null
  },
  /** 组件状态，值为 `true` 表示下拉状态，值为 `false` 表示收起状态，非受控属性 */
  defaultValue: {
    type: Boolean,
    default: false
  },
  /** 下拉高度多少触发，单位rpx */
  loadBarHeight: {
    type: Number,
    default: 120
  },
  bottomValue: {
    type: [Boolean],
    default: null
  }
};
exports.propsdetail = propsdetail;
