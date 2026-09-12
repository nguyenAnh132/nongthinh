import { Injectable } from '@angular/core';

export type ProfileRole = 'FARMER' | 'BRAND' | 'ADMIN';

export interface ProfileField {
  key: string;
  label: string;
  type: string;
  controlType?: 'input' | 'select';
  options?: { value: string | number; label: string }[];
  required: boolean;
  placeholder: string;
}

/**
 * Field keys khớp backend RegisterFarmerRequest DTO.
 * Backend: firstName, lastName, gender, phone, provinceId?, communeId?, addressDetail?, avatarUrl?
 */
const FARMER_FIELDS: ProfileField[] = [
  { key: 'firstName', label: 'Họ', type: 'text', controlType: 'input', required: true, placeholder: 'Nhập họ' },
  { key: 'lastName', label: 'Tên', type: 'text', controlType: 'input', required: true, placeholder: 'Nhập tên' },
  {
    key: 'phone',
    label: 'Số điện thoại',
    type: 'tel',
    controlType: 'input',
    required: false,
    placeholder: 'Nhập 10 chữ số',
  },
  {
    key: 'gender',
    label: 'Giới tính',
    type: 'text',
    controlType: 'select',
    options: [
      { value: 'MALE', label: 'Nam' },
      { value: 'FEMALE', label: 'Nữ' },
      { value: 'OTHER', label: 'Khác' }
    ],
    required: true,
    placeholder: 'Chọn giới tính',
  },
  {
    key: 'provinceId',
    label: 'Tỉnh/Thành phố',
    type: 'text',
    controlType: 'select',
    required: false,
    placeholder: 'Chọn tỉnh/thành phố',
  },
  {
    key: 'communeId',
    label: 'Phường/Xã',
    type: 'text',
    controlType: 'select',
    required: false,
    placeholder: 'Chọn phường/xã',
  },
  {
    key: 'addressDetail',
    label: 'Địa chỉ chi tiết',
    type: 'text',
    controlType: 'input',
    required: false,
    placeholder: 'Nhập địa chỉ',
  },
];

/**
 * Field keys khớp backend RegisterBrandRequest DTO.
 * Backend: brandName, taxCode?, description?, phone, officeProvinceId?,
 *          officeCommuneId?, officeAddressDetail?, representativeName,
 *          representativePhone, representativeEmail, logoUrl?, bannerUrl?,
 *          websiteUrl?
 */
const BRAND_FIELDS: ProfileField[] = [
  {
    key: 'brandName',
    label: 'Tên thương hiệu',
    type: 'text',
    required: true,
    placeholder: 'Nhập tên thương hiệu',
  },
  {
    key: 'taxCode',
    label: 'Mã số thuế',
    type: 'text',
    required: false,
    placeholder: 'Nhập mã số thuế doanh nghiệp',
  },
  {
    key: 'description',
    label: 'Mô tả',
    type: 'text',
    required: false,
    placeholder: 'Mô tả ngắn về thương hiệu (tối đa 1000 ký tự)',
  },
  {
    key: 'phone',
    label: 'Số điện thoại',
    type: 'tel',
    required: true,
    placeholder: 'Nhập 10 chữ số',
  },
  {
    key: 'officeProvinceId',
    label: 'Tỉnh/Thành phố',
    type: 'text',
    controlType: 'select',
    required: false,
    placeholder: 'Chọn tỉnh/thành phố',
  },
  {
    key: 'officeCommuneId',
    label: 'Phường/Xã',
    type: 'text',
    controlType: 'select',
    required: false,
    placeholder: 'Chọn phường/xã',
  },
  {
    key: 'officeAddressDetail',
    label: 'Địa chỉ văn phòng',
    type: 'text',
    required: false,
    placeholder: 'Nhập địa chỉ văn phòng',
  },
  {
    key: 'representativeName',
    label: 'Người đại diện',
    type: 'text',
    required: true,
    placeholder: 'Họ và tên người đại diện',
  },
  {
    key: 'representativePhone',
    label: 'SĐT người đại diện',
    type: 'tel',
    required: true,
    placeholder: 'Nhập 10 chữ số',
  },
  {
    key: 'representativeEmail',
    label: 'Email người đại diện',
    type: 'email',
    required: true,
    placeholder: 'representative@example.com',
  },
  {
    key: 'websiteUrl',
    label: 'Website',
    type: 'text',
    required: false,
    placeholder: 'https://example.com',
  },
];

const ADMIN_FIELDS: ProfileField[] = [
  { key: 'firstName', label: 'Họ', type: 'text', required: true, placeholder: 'Nhập họ' },
  { key: 'lastName', label: 'Tên', type: 'text', required: true, placeholder: 'Nhập tên' },
  {
    key: 'phone',
    label: 'Số điện thoại',
    type: 'tel',
    required: false,
    placeholder: 'Nhập số điện thoại',
  },
];

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  getProfileFields(role: ProfileRole): ProfileField[] {
    switch (role) {
      case 'BRAND':
        return BRAND_FIELDS;
      case 'ADMIN':
        return ADMIN_FIELDS;
      case 'FARMER':
      default:
        return FARMER_FIELDS;
    }
  }
}
