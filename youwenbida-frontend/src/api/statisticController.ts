// @ts-ignore
/* eslint-disable */
import request from '@/request';

/** getAppAnswerResultCount GET /api/app/statistic */
export async function getAppAnswerResultCountUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppAnswerResultCountUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListAppAnswerResultCountDTO_>('/api/app/statistic', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** getAppTop12 GET /api/app/statistic/top/12 */
export async function getAppTop12UsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListAppAnswerCountDTO_>('/api/app/statistic/top/12', {
    method: 'GET',
    ...(options || {}),
  });
}
