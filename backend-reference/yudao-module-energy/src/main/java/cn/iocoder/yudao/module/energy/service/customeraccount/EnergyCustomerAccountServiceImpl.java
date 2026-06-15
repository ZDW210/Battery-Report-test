package cn.iocoder.yudao.module.energy.service.customeraccount;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.EnergyCustomerAccountMenuOptionRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.EnergyCustomerAccountPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.EnergyCustomerAccountRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.EnergyCustomerAccountSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customeraccount.EnergyCustomerAccountDO;
import cn.iocoder.yudao.module.energy.dal.mysql.customeraccount.EnergyCustomerAccountMapper;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.module.system.enums.permission.RoleTypeEnum;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.*;

/**
 * Fleet owner web account Service.
 */
@Service
@Validated
public class EnergyCustomerAccountServiceImpl implements EnergyCustomerAccountService {

    private static final Long ENERGY_ROOT_MENU_ID = 190010000000L;
    private static final Long CUSTOMER_ACCOUNT_MENU_ID = 190010015000L;
    private static final String ROLE_CODE_PREFIX = "energy_customer_owner_";

    @Resource
    private EnergyCustomerAccountMapper customerAccountMapper;
    @Resource
    private EnergyCustomerService customerService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private RoleService roleService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private MenuService menuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCustomerAccount(EnergyCustomerAccountSaveReqVO createReqVO) {
        if (StrUtil.isBlank(createReqVO.getPassword())) {
            throw exception(CUSTOMER_ACCOUNT_PASSWORD_REQUIRED);
        }
        validateMenuSelected(createReqVO.getMenuIds());
        EnergyCustomerDO customer = customerService.validateCustomerExists(createReqVO.getCustomerId());

        Long userId = adminUserService.createUser(buildUserSaveReqVO(createReqVO, null, createReqVO.getPassword()));
        Long roleId = roleService.createRole(buildRoleSaveReqVO(createReqVO, null, customer), RoleTypeEnum.CUSTOM.getType());
        permissionService.assignUserRole(userId, Collections.singleton(roleId));
        permissionService.assignRoleMenu(roleId, buildAssignableMenuIds(createReqVO.getMenuIds()));

        EnergyCustomerAccountDO account = BeanUtils.toBean(createReqVO, EnergyCustomerAccountDO.class);
        account.setSystemUserId(userId);
        account.setRoleId(roleId);
        account.setMenuIds(formatMenuIds(createReqVO.getMenuIds()));
        customerAccountMapper.insert(account);
        return account.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerAccount(EnergyCustomerAccountSaveReqVO updateReqVO) {
        validateMenuSelected(updateReqVO.getMenuIds());
        EnergyCustomerAccountDO account = validateCustomerAccountExists(updateReqVO.getId());
        EnergyCustomerDO customer = customerService.validateCustomerExists(updateReqVO.getCustomerId());

        adminUserService.updateUser(buildUserSaveReqVO(updateReqVO, account.getSystemUserId(), null));
        adminUserService.updateUserStatus(account.getSystemUserId(), updateReqVO.getStatus());
        roleService.updateRole(buildRoleSaveReqVO(updateReqVO, account.getRoleId(), customer));
        permissionService.assignUserRole(account.getSystemUserId(), Collections.singleton(account.getRoleId()));
        permissionService.assignRoleMenu(account.getRoleId(), buildAssignableMenuIds(updateReqVO.getMenuIds()));

        EnergyCustomerAccountDO updateObj = BeanUtils.toBean(updateReqVO, EnergyCustomerAccountDO.class);
        updateObj.setSystemUserId(account.getSystemUserId());
        updateObj.setRoleId(account.getRoleId());
        updateObj.setMenuIds(formatMenuIds(updateReqVO.getMenuIds()));
        customerAccountMapper.updateById(updateObj);
    }

    @Override
    public void resetPassword(Long id, String password) {
        EnergyCustomerAccountDO account = validateCustomerAccountExists(id);
        adminUserService.updateUserPassword(account.getSystemUserId(), password);
    }

    @Override
    public EnergyCustomerAccountRespVO getCustomerAccount(Long id) {
        EnergyCustomerAccountDO account = validateCustomerAccountExists(id);
        return buildRespVO(account, getCustomerMap(Collections.singleton(account.getCustomerId())));
    }

    @Override
    public PageResult<EnergyCustomerAccountRespVO> getCustomerAccountPage(EnergyCustomerAccountPageReqVO pageReqVO) {
        PageResult<EnergyCustomerAccountDO> pageResult = customerAccountMapper.selectPage(pageReqVO);
        Set<Long> customerIds = pageResult.getList().stream()
                .map(EnergyCustomerAccountDO::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, EnergyCustomerDO> customerMap = getCustomerMap(customerIds);
        List<EnergyCustomerAccountRespVO> list = pageResult.getList().stream()
                .map(account -> buildRespVO(account, customerMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public List<EnergyCustomerAccountMenuOptionRespVO> getVisibleMenuOptions() {
        return menuService.getMenuList().stream()
                .filter(menu -> Objects.equals(menu.getParentId(), ENERGY_ROOT_MENU_ID))
                .filter(menu -> !Objects.equals(menu.getId(), CUSTOMER_ACCOUNT_MENU_ID))
                .filter(menu -> Objects.equals(menu.getStatus(), CommonStatusEnum.ENABLE.getStatus()))
                .filter(menu -> Objects.equals(menu.getType(), MenuTypeEnum.MENU.getType())
                        || Objects.equals(menu.getType(), MenuTypeEnum.DIR.getType()))
                .sorted(Comparator.comparing(MenuDO::getSort, Comparator.nullsLast(Integer::compareTo)))
                .map(menu -> BeanUtils.toBean(menu, EnergyCustomerAccountMenuOptionRespVO.class))
                .collect(Collectors.toList());
    }

    private EnergyCustomerAccountDO validateCustomerAccountExists(Long id) {
        EnergyCustomerAccountDO account = customerAccountMapper.selectById(id);
        if (account == null) {
            throw exception(CUSTOMER_ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    private void validateMenuSelected(Set<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            throw exception(CUSTOMER_ACCOUNT_MENU_REQUIRED);
        }
    }

    private UserSaveReqVO buildUserSaveReqVO(EnergyCustomerAccountSaveReqVO reqVO, Long userId, String password) {
        UserSaveReqVO userReqVO = new UserSaveReqVO();
        userReqVO.setId(userId);
        userReqVO.setUsername(reqVO.getUsername());
        userReqVO.setNickname(reqVO.getNickname());
        userReqVO.setMobile(reqVO.getMobile());
        userReqVO.setPassword(password);
        userReqVO.setRemark(buildUserRemark(reqVO));
        return userReqVO;
    }

    private RoleSaveReqVO buildRoleSaveReqVO(EnergyCustomerAccountSaveReqVO reqVO, Long roleId, EnergyCustomerDO customer) {
        RoleSaveReqVO roleReqVO = new RoleSaveReqVO();
        roleReqVO.setId(roleId);
        roleReqVO.setName(StrUtil.maxLength("客户账号-" + customer.getName() + "-" + reqVO.getNickname(), 30));
        roleReqVO.setCode(ROLE_CODE_PREFIX + reqVO.getUsername());
        roleReqVO.setSort(200);
        roleReqVO.setStatus(reqVO.getStatus());
        roleReqVO.setRemark(buildUserRemark(reqVO));
        return roleReqVO;
    }

    private String buildUserRemark(EnergyCustomerAccountSaveReqVO reqVO) {
        return StrUtil.blankToDefault(reqVO.getRemark(), "移动储能客户老板账号");
    }

    private Set<Long> buildAssignableMenuIds(Set<Long> selectedMenuIds) {
        List<MenuDO> menus = menuService.getMenuList();
        Set<Long> selected = new LinkedHashSet<>(selectedMenuIds);
        Set<Long> assignable = new LinkedHashSet<>();
        assignable.add(ENERGY_ROOT_MENU_ID);
        assignable.addAll(selected);
        for (MenuDO menu : menus) {
            if (Objects.equals(menu.getParentId(), ENERGY_ROOT_MENU_ID) && selected.contains(menu.getId())) {
                assignable.add(menu.getId());
                continue;
            }
            if (selected.contains(menu.getParentId()) && isSafeChildPermission(menu)) {
                assignable.add(menu.getId());
            }
        }
        return assignable;
    }

    private boolean isSafeChildPermission(MenuDO menu) {
        String permission = menu.getPermission();
        return StrUtil.isNotBlank(permission) && (permission.endsWith(":query") || permission.endsWith(":export"));
    }

    private String formatMenuIds(Set<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return "";
        }
        return menuIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private Set<Long> parseMenuIds(String menuIds) {
        if (StrUtil.isBlank(menuIds)) {
            return Collections.emptySet();
        }
        return Arrays.stream(menuIds.split(","))
                .filter(StrUtil::isNotBlank)
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private EnergyCustomerAccountRespVO buildRespVO(EnergyCustomerAccountDO account, Map<Long, EnergyCustomerDO> customerMap) {
        EnergyCustomerAccountRespVO respVO = BeanUtils.toBean(account, EnergyCustomerAccountRespVO.class);
        respVO.setMenuIds(parseMenuIds(account.getMenuIds()));
        EnergyCustomerDO customer = customerMap.get(account.getCustomerId());
        if (customer != null) {
            respVO.setCustomerName(customer.getName());
        }
        AdminUserDO user = account.getSystemUserId() == null ? null : adminUserService.getUser(account.getSystemUserId());
        if (user != null) {
            respVO.setUsername(user.getUsername());
            respVO.setNickname(user.getNickname());
            respVO.setMobile(user.getMobile());
            respVO.setStatus(user.getStatus());
        }
        RoleDO role = account.getRoleId() == null ? null : roleService.getRole(account.getRoleId());
        if (role != null) {
            respVO.setRoleId(role.getId());
        }
        return respVO;
    }

    private Map<Long, EnergyCustomerDO> getCustomerMap(Collection<Long> customerIds) {
        if (CollUtil.isEmpty(customerIds)) {
            return Collections.emptyMap();
        }
        return customerService.getCustomerSimpleList().stream()
                .filter(customer -> customerIds.contains(customer.getId()))
                .collect(Collectors.toMap(EnergyCustomerDO::getId, customer -> customer, (first, second) -> first));
    }

}
