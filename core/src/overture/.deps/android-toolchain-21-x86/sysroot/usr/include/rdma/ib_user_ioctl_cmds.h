/****************************************************************************
 ****************************************************************************
 ***
 ***   This header was automatically generated from a Linux kernel header
 ***   of the same name, to make information necessary for userspace to
 ***   call into the kernel available to libc.  It contains only constants,
 ***   structures, and macros generated from the original header, and thus,
 ***   contains no copyrightable information.
 ***
 ***   To edit the content of this header, modify the corresponding
 ***   source file (e.g. under external/kernel-headers/original/) then
 ***   run bionic/libc/kernel/tools/update_all.py
 ***
 ***   Any manual change here will be lost the next time this script will
 ***   be run. You've been warned!
 ***
 ****************************************************************************
 ****************************************************************************/
#ifndef IB_USER_IOCTL_CMDS_H
#define IB_USER_IOCTL_CMDS_H
#define UVERBS_ID_NS_MASK 0xF000
#define UVERBS_ID_NS_SHIFT 12
#define UVERBS_UDATA_DRIVER_DATA_NS 1
#define UVERBS_UDATA_DRIVER_DATA_FLAG (1UL << UVERBS_ID_NS_SHIFT)
enum uverbs_default_objects {
  UVERBS_OBJECT_DEVICE,
  UVERBS_OBJECT_PD,
  UVERBS_OBJECT_COMP_CHANNEL,
  UVERBS_OBJECT_CQ,
  UVERBS_OBJECT_QP,
  UVERBS_OBJECT_SRQ,
  UVERBS_OBJECT_AH,
  UVERBS_OBJECT_MR,
  UVERBS_OBJECT_MW,
  UVERBS_OBJECT_FLOW,
  UVERBS_OBJECT_XRCD,
  UVERBS_OBJECT_RWQ_IND_TBL,
  UVERBS_OBJECT_WQ,
  UVERBS_OBJECT_FLOW_ACTION,
  UVERBS_OBJECT_DM,
};
enum {
  UVERBS_ATTR_UHW_IN = UVERBS_UDATA_DRIVER_DATA_FLAG,
  UVERBS_ATTR_UHW_OUT,
};
enum uverbs_attrs_create_cq_cmd_attr_ids {
  UVERBS_ATTR_CREATE_CQ_HANDLE,
  UVERBS_ATTR_CREATE_CQ_CQE,
  UVERBS_ATTR_CREATE_CQ_USER_HANDLE,
  UVERBS_ATTR_CREATE_CQ_COMP_CHANNEL,
  UVERBS_ATTR_CREATE_CQ_COMP_VECTOR,
  UVERBS_ATTR_CREATE_CQ_FLAGS,
  UVERBS_ATTR_CREATE_CQ_RESP_CQE,
};
enum uverbs_attrs_destroy_cq_cmd_attr_ids {
  UVERBS_ATTR_DESTROY_CQ_HANDLE,
  UVERBS_ATTR_DESTROY_CQ_RESP,
};
enum uverbs_attrs_create_flow_action_esp {
  UVERBS_ATTR_FLOW_ACTION_ESP_HANDLE,
  UVERBS_ATTR_FLOW_ACTION_ESP_ATTRS,
  UVERBS_ATTR_FLOW_ACTION_ESP_ESN,
  UVERBS_ATTR_FLOW_ACTION_ESP_KEYMAT,
  UVERBS_ATTR_FLOW_ACTION_ESP_REPLAY,
  UVERBS_ATTR_FLOW_ACTION_ESP_ENCAP,
};
enum uverbs_attrs_destroy_flow_action_esp {
  UVERBS_ATTR_DESTROY_FLOW_ACTION_HANDLE,
};
enum uverbs_methods_cq {
  UVERBS_METHOD_CQ_CREATE,
  UVERBS_METHOD_CQ_DESTROY,
};
enum uverbs_methods_actions_flow_action_ops {
  UVERBS_METHOD_FLOW_ACTION_ESP_CREATE,
  UVERBS_METHOD_FLOW_ACTION_DESTROY,
  UVERBS_METHOD_FLOW_ACTION_ESP_MODIFY,
};
enum uverbs_attrs_alloc_dm_cmd_attr_ids {
  UVERBS_ATTR_ALLOC_DM_HANDLE,
  UVERBS_ATTR_ALLOC_DM_LENGTH,
  UVERBS_ATTR_ALLOC_DM_ALIGNMENT,
};
enum uverbs_attrs_free_dm_cmd_attr_ids {
  UVERBS_ATTR_FREE_DM_HANDLE,
};
enum uverbs_methods_dm {
  UVERBS_METHOD_DM_ALLOC,
  UVERBS_METHOD_DM_FREE,
};
enum uverbs_attrs_reg_dm_mr_cmd_attr_ids {
  UVERBS_ATTR_REG_DM_MR_HANDLE,
  UVERBS_ATTR_REG_DM_MR_OFFSET,
  UVERBS_ATTR_REG_DM_MR_LENGTH,
  UVERBS_ATTR_REG_DM_MR_PD_HANDLE,
  UVERBS_ATTR_REG_DM_MR_ACCESS_FLAGS,
  UVERBS_ATTR_REG_DM_MR_DM_HANDLE,
  UVERBS_ATTR_REG_DM_MR_RESP_LKEY,
  UVERBS_ATTR_REG_DM_MR_RESP_RKEY,
};
enum uverbs_methods_mr {
  UVERBS_METHOD_DM_MR_REG,
};
#endif
