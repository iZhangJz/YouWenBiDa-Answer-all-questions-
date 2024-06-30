/**
 * 定义通用枚举
 */

export const APP_TYPE_MAP: { [key: number]: string } = {
  1: "测评类",
  0: "得分类",
};

export const APP_TYPE_ENUM: { [key: string]: number } = {
  TEST: 1,
  SCORE: 0,
};

export const REVIEW_STATUS_MAP: { [key: number]: string } = {
  0: "未审核",
  1: "通过",
  2: "拒绝",
};

export const REVIEW_STATUS_ENUM: { [key: string]: number } = {
  WAIT: 0,
  PASS: 1,
  REFUSE: 2,
};

export const SCORING_STRATEGY_MAP: { [key: number]: string } = {
  0: "自定义",
  1: "AI",
};

export const SCORING_STRATEGY_ENUM: { [key: string]: number } = {
  CUSTOMIZE: 0,
  AI: 1,
};
