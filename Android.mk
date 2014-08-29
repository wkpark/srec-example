LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
res_dirs := res

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := srec-example

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))
LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_JNI_SHARED_LIBRARIES := libsrec_jni

LOCAL_REQUIRED_MODULES := libsrec_jni

include $(BUILD_PACKAGE)

# This finds and builds the test apk as well, so a single make does both.
include $(call all-makefiles-under,$(LOCAL_PATH))
