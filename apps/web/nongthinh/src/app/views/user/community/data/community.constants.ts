import { PostCategory } from '../models/community.models';

export const POST_CATEGORY_META: Record<
  string,
  { label: string; shortLabel: string; iconUrl: string }
> = {
  QUESTION: {
    label: 'Hỏi đáp kỹ thuật',
    shortLabel: 'Hỏi đáp',
    iconUrl: '/icons/community/user-question.png',
  },
  EXPERIENCE: {
    label: 'Chia sẻ kinh nghiệm',
    shortLabel: 'Kinh nghiệm',
    iconUrl: '/icons/community/interactive.png',
  },
  WARNING: {
    label: 'Cảnh báo mùa vụ',
    shortLabel: 'Cảnh báo',
    iconUrl: '/icons/community/bell.png',
  },
  PRICE: {
    label: 'Thông tin giá nông sản',
    shortLabel: 'Giá nông sản',
    iconUrl: '/icons/community/globe.png',
  },
  MARKET: {
    label: 'Đăng bán nông sản',
    shortLabel: 'Chợ nông sản',
    iconUrl: '/icons/community/globe.png',
  },
  MODEL: {
    label: 'Mô hình canh tác',
    shortLabel: 'Mô hình hay',
    iconUrl: '/icons/community/interactive.png',
  },
  EVENT: {
    label: 'Sự kiện',
    shortLabel: 'Sự kiện',
    iconUrl: '/icons/community/calendar-star.png',
  },
};

export const DEFAULT_POST_CATEGORY_META = {
  label: 'Bài viết cộng đồng',
  shortLabel: 'Cộng đồng',
  iconUrl: '/icons/community/interactive.png',
};
