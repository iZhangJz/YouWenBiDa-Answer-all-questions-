import ACCESS_ROLE_ENUM from "@/access/accessRoleEnum";

/**
 * 检查用户是否有权限访问指定页面
 * @param user
 * @param needRole
 */
const checkAccess = (
  user: API.LoginUserVO,
  needRole = [ACCESS_ROLE_ENUM.NOT_LOGIN]
) => {
  const loginUserRole = user.userRole ?? ACCESS_ROLE_ENUM.NOT_LOGIN;
  if (needRole.includes(ACCESS_ROLE_ENUM.NOT_LOGIN)) {
    // 无论用户权限如何，都放行
    return true;
  }
  if (loginUserRole === ACCESS_ROLE_ENUM.NOT_LOGIN) {
    // 当前用户未登录; 如果跳转到的页面不需要登录则放行
    return needRole.includes(ACCESS_ROLE_ENUM.NOT_LOGIN);
  }
  // 当前用户已经登录
  return needRole.includes(loginUserRole);
};

export default checkAccess;
