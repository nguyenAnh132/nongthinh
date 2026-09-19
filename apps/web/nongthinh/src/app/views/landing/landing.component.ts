import {
  Component,
  ElementRef,
  DestroyRef,
  OnInit,
  afterNextRender,
  inject,
  PLATFORM_ID,
  HostListener,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent implements OnInit {
  private readonly el = inject(ElementRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly authService = inject(AuthService);
  private ctx: gsap.Context | null = null;

  /* ─── Mobile menu state ───────────────────────────── */
  mobileMenuOpen = false;

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    this.authService.ensureMeLoaded().subscribe((isAuthenticated) => {
      if (isAuthenticated || this.authService.registrationRequired()) {
        this.authService.navigateAfterLogin();
      }
    });
  }

  /* ─── Testimonial slider state ─────────────────────── */
  activeTestimonial = 0;
  private testimonialInterval: ReturnType<typeof setInterval> | null = null;

  /* ─── Particle data ──────────────────────────────────── */
  heroParticles = [
    { id: 1, class: 'particle--leaf', x: 8, y: 15, size: 16 },
    { id: 2, class: 'particle--dot', x: 22, y: 55, size: 8 },
    { id: 3, class: 'particle--glow', x: 12, y: 70, size: 120 },
    { id: 4, class: 'particle--leaf', x: 38, y: 25, size: 12 },
    { id: 5, class: 'particle--dot', x: 52, y: 78, size: 6 },
    { id: 6, class: 'particle--glow', x: 78, y: 12, size: 100 },
    { id: 7, class: 'particle--leaf', x: 68, y: 45, size: 14 },
    { id: 8, class: 'particle--dot', x: 88, y: 30, size: 7 },
    { id: 9, class: 'particle--leaf', x: 92, y: 65, size: 18 },
    { id: 10, class: 'particle--dot', x: 45, y: 10, size: 5 },
    { id: 11, class: 'particle--glow', x: 55, y: 50, size: 80 },
    { id: 12, class: 'particle--leaf', x: 28, y: 82, size: 10 },
  ];

  /* ─── Benefits data ──────────────────────────────────── */
  benefits = [
    {
      icon: '',
      iconClass: 'benefit-icon--ai',
      title: 'AI phân tích bệnh lúa',
      desc: 'Chụp ảnh lá lúa, AI phân tích và đưa ra chẩn đoán chính xác trong vài giây. Hỗ trợ hơn 50 loại bệnh phổ biến.',
    },
    {
      icon: '',
      iconClass: 'benefit-icon--schedule',
      title: 'Đặt lịch phun thuốc',
      desc: 'Đặt lịch dịch vụ phun thuốc chuyên nghiệp bằng drone và máy phun tự động, tiết kiệm thời gian và chi phí.',
    },
    {
      icon: '',
      iconClass: 'benefit-icon--land',
      title: 'Mua bán đất nông nghiệp',
      desc: 'Nền tảng giao dịch đất nông nghiệp uy tín, minh bạch. Cho thuê hoặc mua bán dễ dàng với pháp lý rõ ràng.',
    },
    {
      icon: '',
      iconClass: 'benefit-icon--scale',
      title: 'Công cụ cân lúa',
      desc: 'Công cụ tính toán sản lượng, cân đo và theo dõi giá lúa thị trường real-time giúp bà con nắm bắt cơ hội.',
    },
  ];

  /* ─── Campaigns data (NEW) ─────────────────────────── */
  campaigns = [
    {
      image: 'images/green-campaign.png',
      category: 'Môi trường',
      title: 'Trồng 10,000 cây xanh cho đồng bằng sông Cửu Long',
      desc: 'Chiến dịch phủ xanh vùng đất trống, chống xâm nhập mặn và tạo lá chắn gió cho đồng ruộng.',
      progress: 72,
      goal: '10,000 cây',
    },
    {
      image: 'images/drone-farming.png',
      category: 'Công nghệ',
      title: 'Drone phun thuốc miễn phí cho 500 hộ nông dân',
      desc: 'Hỗ trợ nông dân tiếp cận công nghệ phun thuốc hiện đại, giảm 90% lượng nước sử dụng.',
      progress: 48,
      goal: '500 hộ dân',
    },
    {
      image: 'images/ai-agriculture.png',
      category: 'AI & Nông nghiệp',
      title: 'AI chẩn đoán bệnh lúa miễn phí toàn quốc',
      desc: 'Mở rộng dịch vụ AI phân tích bệnh lúa đến tất cả 63 tỉnh thành Việt Nam.',
      progress: 85,
      goal: '63 tỉnh thành',
    },
  ];

  /* ─── Expert advice data ─────────────────────────────── */
  expertAdvices = [
    {
      category: 'Phòng bệnh',
      title: 'Phòng trừ bệnh đạo ôn hiệu quả cho vụ Đông Xuân',
      content:
        'Bệnh đạo ôn là một trong những bệnh nguy hiểm nhất trên lúa. Cần phun phòng sớm từ giai đoạn đẻ nhánh, kết hợp bón phân cân đối.',
      name: 'TS. Nguyễn Văn Hùng',
      role: 'Viện Khoa học Nông nghiệp Việt Nam',
      avatar: 'NH',
      imgBg:
        'linear-gradient(135deg, #1a8a45 0%, #27ae60 50%, #6ddb96 100%)',
    },
    {
      category: 'Dinh dưỡng',
      title: 'Bí quyết bón phân đúng cách tăng năng suất 30%',
      content:
        'Bón phân theo nguyên tắc 4 đúng: đúng loại, đúng liều, đúng lúc, đúng cách. Kết hợp phân hữu cơ và vô cơ để cải tạo đất bền vững.',
      name: 'PGS.TS Trần Thị Mai',
      role: 'Đại học Nông Lâm TP.HCM',
      avatar: 'TM',
      imgBg:
        'linear-gradient(135deg, #e67e22 0%, #f39c12 50%, #f7dc6f 100%)',
    },
    {
      category: 'Công nghệ',
      title: 'Ứng dụng drone trong nông nghiệp hiện đại',
      content:
        'Drone phun thuốc giúp giảm 90% lượng nước, tiết kiệm 50% thuốc và tăng hiệu quả xử lý gấp 30 lần so với phương pháp truyền thống.',
      name: 'KS. Lê Minh Tuấn',
      role: 'Chuyên gia AgriTech',
      avatar: 'LT',
      imgBg:
        'linear-gradient(135deg, #2980b9 0%, #3498db 50%, #85c1e9 100%)',
    },
  ];

  /* ─── Projects data (NEW) ──────────────────────────── */
  projects = [
    {
      image: 'images/drone-farming.png',
      category: 'Công nghệ cao',
      location: 'Đồng bằng sông Cửu Long',
      title: 'Ứng dụng Drone phun thuốc thông minh cho vùng lúa trọng điểm',
      desc: 'Triển khai đội drone DJI T50 phun thuốc tự động theo bản đồ GPS, giúp giảm 90% nước, 50% thuốc BVTV và tăng hiệu suất xử lý gấp 30 lần so với phương pháp truyền thống.',
    },
    {
      image: 'images/ai-agriculture.png',
      category: 'AI & Machine Learning',
      location: 'Toàn quốc',
      title: 'AI phân tích bệnh lúa — Chẩn đoán chính xác 95% trong 3 giây',
      desc: 'Mô hình AI được huấn luyện trên 2 triệu ảnh lá lúa Việt Nam, nhận diện hơn 50 loại bệnh. Nông dân chỉ cần chụp ảnh bằng điện thoại, AI sẽ đưa ra kết quả và hướng dẫn xử lý ngay lập tức.',
    },
  ];

  /* ─── Testimonials data (NEW) ──────────────────────── */
  testimonials = [
    {
      name: 'Anh Nguyễn Văn Tám',
      role: 'Nông dân, Long An',
      initials: 'NT',
      avatarBg: 'linear-gradient(135deg, #27ae60, #2ecc71)',
      content:
        'Từ khi dùng Nông Thịnh, tôi tiết kiệm được 40% chi phí thuốc BVTV. AI chẩn đoán bệnh đạo ôn chính xác, giúp tôi phun đúng thời điểm. Mùa vụ vừa rồi năng suất tăng 25%!',
      rating: 5,
    },
    {
      name: 'Chị Trần Thị Hoa',
      role: 'Nông dân, An Giang',
      initials: 'TH',
      avatarBg: 'linear-gradient(135deg, #e67e22, #f39c12)',
      content:
        'Đặt lịch phun thuốc bằng drone qua Nông Thịnh rất tiện lợi. Trước đây tôi phải thuê người phun mất 2 ngày, giờ drone làm chỉ trong 2 tiếng. Ứng dụng dễ dùng, bà con ai cũng xài được.',
      rating: 5,
    },
    {
      name: 'Anh Lê Hoàng Phú',
      role: 'Nông dân, Đồng Tháp',
      initials: 'LP',
      avatarBg: 'linear-gradient(135deg, #2980b9, #3498db)',
      content:
        'Tính năng theo dõi giá lúa real-time giúp tôi bán được giá tốt hơn. Công cụ cân lúa tính toán chính xác sản lượng. Cảm ơn Nông Thịnh đã đồng hành cùng nông dân!',
      rating: 5,
    },
    {
      name: 'Chú Phạm Văn Đức',
      role: 'Nông dân, Kiên Giang',
      initials: 'PD',
      avatarBg: 'linear-gradient(135deg, #8e44ad, #9b59b6)',
      content:
        'Tôi 62 tuổi mà vẫn dùng được Nông Thịnh dễ dàng. Giao diện đơn giản, chữ to rõ ràng. Con trai tôi cài cho, giờ tôi tự biết xem dự báo sâu bệnh và đặt lịch phun thuốc.',
      rating: 4,
    },
  ];

  /* ─── Pesticides data ────────────────────────────────── */
  pesticides = [
    {
      name: 'Amistar Top 325SC',
      brand: 'Syngenta Vietnam',
      icon: '🧪',
      desc: 'Thuốc trừ bệnh phổ rộng, hiệu quả cao trên đạo ôn, khô vằn và lem lép hạt.',
      rating: 95,
      users: '12,500+',
      badge: 'Bán chạy #1',
      bgGradient: 'linear-gradient(135deg, #edfaf3 0%, #d4f5e2 100%)',
    },
    {
      name: 'Chess 50WG',
      brand: 'Mitsui Chemicals',
      icon: '🛡️',
      desc: 'Thuốc trừ rầy nâu thế hệ mới, tác động nhanh, an toàn cho thiên địch.',
      rating: 92,
      users: '9,800+',
      badge: 'Hot',
      bgGradient: 'linear-gradient(135deg, #e8f4fd 0%, #d1ecf9 100%)',
    },
    {
      name: 'Tilt Super 300EC',
      brand: 'Syngenta Vietnam',
      icon: '💊',
      desc: 'Đặc trị bệnh đạo ôn cổ bông, khô vằn. Hiệu quả kéo dài, bảo vệ toàn diện.',
      rating: 90,
      users: '8,200+',
      badge: null,
      bgGradient: 'linear-gradient(135deg, #fef9e7 0%, #fbeaa6 100%)',
    },
    {
      name: 'Regent 800WG',
      brand: 'BASF Vietnam',
      icon: '',
      desc: 'Thuốc trừ sâu cuốn lá, sâu đục thân hiệu quả. Công nghệ phân tán nước tiên tiến.',
      rating: 88,
      users: '7,500+',
      badge: null,
      bgGradient: 'linear-gradient(135deg, #f4ecf7 0%, #e8d8f0 100%)',
    },
    {
      name: 'Sofit 300EC',
      brand: 'Syngenta Vietnam',
      icon: '🌿',
      desc: 'Thuốc trừ cỏ tiền nảy mầm, an toàn cho lúa. Diệt sạch cỏ dại từ gốc.',
      rating: 87,
      users: '6,900+',
      badge: null,
      bgGradient: 'linear-gradient(135deg, #edfaf3 0%, #a8e6c1 100%)',
    },
    {
      name: 'Antracol 70WP',
      brand: 'Bayer CropScience',
      icon: '',
      desc: 'Thuốc trừ bệnh tiếp xúc, cung cấp kẽm cho cây. Phòng ngừa nhiều loại nấm bệnh.',
      rating: 85,
      users: '6,100+',
      badge: null,
      bgGradient: 'linear-gradient(135deg, #fef9e7 0%, #f7dc6f 100%)',
    },
  ];

  /* ─── Stats data ─────────────────────────────────────── */
  stats = [
    { icon: '', value: 10000, suffix: '+', label: 'Nông dân tin dùng' },
    { icon: '', value: 500, suffix: '+', label: 'Chuyên gia tư vấn' },
    { icon: '', value: 50, suffix: '+', label: 'Tỉnh thành phủ sóng' },
    { icon: '', value: 99, suffix: '%', label: 'Tỷ lệ hài lòng' },
  ];

  /* ─── Features data ──────────────────────────────────── */
  features = [
    {
      icon: '',
      title: 'AI thông minh',
      desc: 'Trí tuệ nhân tạo được huấn luyện trên hàng triệu mẫu dữ liệu nông nghiệp Việt Nam.',
    },
    {
      icon: '',
      title: 'Bảo mật tuyệt đối',
      desc: 'Dữ liệu được mã hóa AES-256, tuân thủ tiêu chuẩn bảo mật quốc tế ISO 27001.',
    },
    {
      icon: '',
      title: 'Đa nền tảng',
      desc: 'Sử dụng mọi lúc mọi nơi trên Web, iOS và Android. Giao diện thân thiện, dễ dùng.',
    },
    {
      icon: '',
      title: 'Cộng đồng lớn mạnh',
      desc: 'Kết nối với hàng nghìn nông dân và chuyên gia trên khắp Việt Nam, chia sẻ kinh nghiệm.',
    },
    {
      icon: '',
      title: 'Tiết kiệm chi phí',
      desc: 'Giảm đến 40% chi phí vật tư nông nghiệp nhờ phân tích chính xác và tối ưu hóa quy trình.',
    },
    {
      icon: '',
      title: 'Báo cáo chi tiết',
      desc: 'Dashboard trực quan với biểu đồ phân tích năng suất, chi phí và lợi nhuận theo mùa vụ.',
    },
  ];

  /* ─── Navbar scroll state ────────────────────────────── */
  private isScrolled = false;

  @HostListener('window:scroll')
  onWindowScroll(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    const navbar = this.el.nativeElement.querySelector('#navbar');
    const topBar = this.el.nativeElement.querySelector('#top-bar');
    const scrollBtn = this.el.nativeElement.querySelector('#scroll-to-top');
    if (!navbar) return;
    const scrolled = window.scrollY > 60;
    if (scrolled !== this.isScrolled) {
      this.isScrolled = scrolled;
      if (scrolled) {
        navbar.classList.add('scrolled');
        topBar?.classList.add('hidden');
      } else {
        navbar.classList.remove('scrolled');
        topBar?.classList.remove('hidden');
      }
    }
    // Scroll to top button
    if (scrollBtn) {
      if (window.scrollY > 500) {
        scrollBtn.classList.add('visible');
      } else {
        scrollBtn.classList.remove('visible');
      }
    }
  }

  constructor() {
    afterNextRender(() => {
      if (!isPlatformBrowser(this.platformId)) return;
      // Ensure hero video plays after SSR hydration (browser autoplay policy)
      const video = this.el.nativeElement.querySelector(
        '.hero-bg-img'
      ) as HTMLVideoElement | null;
      if (video) {
        video.muted = true;
        video.play().catch(() => {
          // Autoplay blocked — silently ignore, video stays as static frame
        });
      }
      this.initGSAP();
      this.startTestimonialSlider();
    });

    this.destroyRef.onDestroy(() => {
      this.ctx?.revert();
      if (this.testimonialInterval) {
        clearInterval(this.testimonialInterval);
      }
    });
  }

  /* ─── Mobile Menu ────────────────────────────────────── */
  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  /* ─── Auth ───────────────────────────────────────────── */
  login(): void {
    this.authService.login();
  }

  /* ─── Scroll to Top ──────────────────────────────────── */
  scrollToTop(): void {
    if (isPlatformBrowser(this.platformId)) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  /* ─── Testimonial Slider ─────────────────────────────── */
  goToTestimonial(index: number): void {
    this.activeTestimonial = index;
    this.updateTestimonialSlider();
  }

  private startTestimonialSlider(): void {
    this.testimonialInterval = setInterval(() => {
      this.activeTestimonial =
        (this.activeTestimonial + 1) % this.testimonials.length;
      this.updateTestimonialSlider();
    }, 5000);
  }

  private updateTestimonialSlider(): void {
    const track = this.el.nativeElement.querySelector(
      '#testimonials-track'
    ) as HTMLElement | null;
    if (!track) return;
    const card = track.querySelector('.testimonial-card') as HTMLElement | null;
    if (!card) return;
    const gap = 32; // matches CSS gap
    const cardWidth = card.offsetWidth + gap;
    gsap.to(track, {
      x: -this.activeTestimonial * cardWidth,
      duration: 0.6,
      ease: 'power2.out',
    });
  }

  /* ═══════════════════════════════════════════════════════
     GSAP INITIALIZATION
     ═══════════════════════════════════════════════════════ */
  private initGSAP(): void {
    gsap.registerPlugin(ScrollTrigger);

    const mm = gsap.matchMedia();

    mm.add(
      {
        isDesktop: '(min-width: 769px)',
        isMobile: '(max-width: 768px)',
        reduceMotion: '(prefers-reduced-motion: reduce)',
      },
      (context) => {
        const { reduceMotion } = context.conditions!;
        const dur = reduceMotion ? 0 : undefined;

        this.ctx = gsap.context(() => {
          this.animateHero(dur);
          this.animateParticles(dur);
          this.animateSections(dur);
          this.animateBenefits(dur);
          this.animateCampaigns(dur);
          this.animateAdvice(dur);
          this.animateProjects(dur);
          this.animateTestimonials(dur);
          this.animatePesticides(dur);
          this.animateWhyUs(dur);
          this.init3DTilt();
        }, this.el.nativeElement);

        return () => {
          this.ctx?.revert();
        };
      }
    );
  }

  /* ─── Hero Animation ─────────────────────────────────── */
  private animateHero(dur?: number): void {
    const tl = gsap.timeline({
      defaults: { duration: dur ?? 0.8, ease: 'power3.out', overwrite: 'auto' },
    });

    tl.fromTo(
        '.hero-title-line',
        { autoAlpha: 0, y: 60 },
        { autoAlpha: 1, y: 0, duration: dur ?? 1, stagger: 0.2 }
      )
      .fromTo(
        '.hero-subtitle',
        { autoAlpha: 0, y: 30 },
        { autoAlpha: 1, y: 0 },
        '-=0.5'
      )
      .fromTo(
        '.hero-actions .btn',
        { autoAlpha: 0, y: 20 },
        { autoAlpha: 1, y: 0, stagger: 0.15 },
        '-=0.4'
      )
      .fromTo(
        '.hero-scroll-indicator',
        { autoAlpha: 0, y: 20 },
        { autoAlpha: 1, y: 0, duration: dur ?? 0.6 },
        '-=0.2'
      )
      .fromTo(
        '.hero-shape',
        { autoAlpha: 0, scale: 0.3 },
        {
          autoAlpha: 1,
          scale: 1,
          duration: dur ?? 1.2,
          stagger: 0.2,
          ease: 'elastic.out(1, 0.5)',
        },
        '-=0.8'
      );

    // Parallax on hero background
    gsap.to('.hero-bg-img', {
      y: 80,
      ease: 'none',
      scrollTrigger: {
        trigger: '.hero',
        start: 'top top',
        end: 'bottom top',
        scrub: true,
      },
    });

    // Parallax on hero shapes
    gsap.to('.hero-shape--1', {
      y: -60,
      rotation: 15,
      ease: 'none',
      scrollTrigger: {
        trigger: '.hero',
        start: 'top top',
        end: 'bottom top',
        scrub: true,
      },
    });

    gsap.to('.hero-shape--2', {
      y: -40,
      rotation: -10,
      ease: 'none',
      scrollTrigger: {
        trigger: '.hero',
        start: 'top top',
        end: 'bottom top',
        scrub: true,
      },
    });
  }

  /* ─── Floating Particles ─────────────────────────────── */
  private animateParticles(dur?: number): void {
    if (dur === 0) return; // skip for reduced motion
    const particles = this.el.nativeElement.querySelectorAll(
      '.particle'
    ) as NodeListOf<HTMLElement>;
    particles.forEach((p: HTMLElement, i: number) => {
      const duration = 3 + Math.random() * 4;
      const xMove = -20 + Math.random() * 40;
      const yMove = -20 + Math.random() * 40;

      gsap.to(p, {
        x: xMove,
        y: yMove,
        rotation: Math.random() * 360,
        duration: duration,
        repeat: -1,
        yoyo: true,
        ease: 'sine.inOut',
        delay: i * 0.3,
      });
    });
  }

  /* ─── Section Reveal ─────────────────────────────────── */
  private animateSections(dur?: number): void {
    const sections = this.el.nativeElement.querySelectorAll(
      '.section-reveal'
    ) as NodeListOf<HTMLElement>;
    sections.forEach((el: HTMLElement) => {
      const headerChildren = el.querySelectorAll('.section-header > *');
      if (headerChildren.length === 0) return;
      gsap.fromTo(headerChildren, 
        { autoAlpha: 0, y: 40 },
        {
          autoAlpha: 1,
          y: 0,
          stagger: 0.12,
          duration: dur ?? 0.8,
          ease: 'power2.out',
          overwrite: 'auto',
          scrollTrigger: {
            trigger: el,
            start: 'top 80%',
            toggleActions: 'play none none reverse',
          },
        }
      );
    });
  }

  /* ─── Benefits Animation ─────────────────────────────── */
  private animateBenefits(dur?: number): void {
    gsap.set('.benefit-card', { autoAlpha: 0, y: 60, scale: 0.95 });
    ScrollTrigger.batch('.benefit-card', {
      onEnter: (elements: Element[]) => {
        gsap.to(elements, {
          autoAlpha: 1,
          y: 0,
          scale: 1,
          stagger: 0.12,
          duration: dur ?? 0.7,
          ease: 'back.out(1.7)',
          overwrite: 'auto',
        });
      },
      start: 'top 85%',
      once: true,
    });

    // Benefit icon entrance
    const icons = this.el.nativeElement.querySelectorAll(
      '.benefit-icon'
    ) as NodeListOf<HTMLElement>;
    icons.forEach((icon: HTMLElement) => {
      gsap.fromTo(icon, 
        { scale: 0, rotation: -180 },
        {
          scale: 1,
          rotation: 0,
          duration: dur ?? 0.8,
          ease: 'back.out(1.7)',
          overwrite: 'auto',
          scrollTrigger: {
            trigger: icon,
            start: 'top 85%',
            toggleActions: 'play none none none',
          },
        }
      );
    });
  }

  /* ─── Campaigns Animation (NEW) ──────────────────────── */
  private animateCampaigns(dur?: number): void {
    gsap.set('.campaign-card', { autoAlpha: 0, y: 50 });
    ScrollTrigger.batch('.campaign-card', {
      onEnter: (elements: Element[]) => {
        gsap.to(elements, {
          autoAlpha: 1,
          y: 0,
          stagger: 0.15,
          duration: dur ?? 0.8,
          ease: 'power2.out',
          overwrite: 'auto',
        });
      },
      start: 'top 85%',
      once: true,
    });

    // Progress bar fill animation
    const progressBars = this.el.nativeElement.querySelectorAll(
      '.campaign-progress-fill'
    ) as NodeListOf<HTMLElement>;
    progressBars.forEach((bar: HTMLElement) => {
      const targetWidth = bar.getAttribute('data-progress') || '0';
      gsap.to(bar, {
        width: targetWidth + '%',
        duration: dur ?? 1.5,
        ease: 'power2.out',
        scrollTrigger: {
          trigger: bar,
          start: 'top 90%',
          toggleActions: 'play none none none',
        },
      });
    });
  }

  /* ─── Expert Advice Animation ────────────────────────── */
  private animateAdvice(dur?: number): void {
    const cards = this.el.nativeElement.querySelectorAll(
      '.advice-card'
    ) as NodeListOf<HTMLElement>;
    cards.forEach((card: HTMLElement, i: number) => {
      gsap.fromTo(card, 
        { autoAlpha: 0, y: 50, rotationX: 10 },
        {
          autoAlpha: 1,
          y: 0,
          rotationX: 0,
          duration: dur ?? 0.8,
          delay: i * 0.15,
          ease: 'power2.out',
          overwrite: 'auto',
          scrollTrigger: {
            trigger: card,
            start: 'top 85%',
            toggleActions: 'play none none reverse',
          },
        }
      );
    });
  }

  /* ─── Projects Animation (NEW) ──────────────────────── */
  private animateProjects(dur?: number): void {
    const items = this.el.nativeElement.querySelectorAll(
      '.project-item'
    ) as NodeListOf<HTMLElement>;
    items.forEach((item: HTMLElement) => {
      const thumb = item.querySelector('.project-thumb');
      const content = item.querySelector('.project-content');

      if (thumb) {
        gsap.fromTo(thumb, 
          { autoAlpha: 0, x: -60 },
          {
            autoAlpha: 1,
            x: 0,
            duration: dur ?? 0.8,
            ease: 'power2.out',
            overwrite: 'auto',
            scrollTrigger: {
              trigger: item,
              start: 'top 80%',
              toggleActions: 'play none none reverse',
            },
          }
        );
      }

      if (content) {
        gsap.fromTo(content, 
          { autoAlpha: 0, x: 60 },
          {
            autoAlpha: 1,
            x: 0,
            duration: dur ?? 0.8,
            ease: 'power2.out',
            overwrite: 'auto',
            scrollTrigger: {
              trigger: item,
              start: 'top 80%',
              toggleActions: 'play none none reverse',
            },
          }
        );
      }
    });
  }

  /* ─── Testimonials Animation (NEW) ─────────────────── */
  private animateTestimonials(dur?: number): void {
    gsap.fromTo('.testimonials-slider', 
      { autoAlpha: 0, y: 40 },
      {
        autoAlpha: 1,
        y: 0,
        duration: dur ?? 0.8,
        ease: 'power2.out',
        overwrite: 'auto',
        scrollTrigger: {
          trigger: '.testimonials',
          start: 'top 80%',
          toggleActions: 'play none none reverse',
        },
      }
    );
  }

  /* ─── Pesticides Animation ───────────────────────────── */
  private animatePesticides(dur?: number): void {
    gsap.set('.pesticide-card', { autoAlpha: 0, y: 50 });
    ScrollTrigger.batch('.pesticide-card', {
      onEnter: (elements: Element[]) => {
        gsap.to(elements, {
          autoAlpha: 1,
          y: 0,
          stagger: 0.1,
          duration: dur ?? 0.7,
          ease: 'power2.out',
          overwrite: 'auto',
        });
      },
      start: 'top 85%',
      once: true,
    });

    // Rating bar fill animation
    const ratingBars = this.el.nativeElement.querySelectorAll(
      '.rating-fill'
    ) as NodeListOf<HTMLElement>;
    ratingBars.forEach((bar: HTMLElement) => {
      const targetWidth = bar.getAttribute('data-rating') || '0';
      gsap.to(bar, {
        width: targetWidth + '%',
        duration: dur ?? 1.2,
        ease: 'power2.out',
        scrollTrigger: {
          trigger: bar,
          start: 'top 90%',
          toggleActions: 'play none none none',
        },
      });
    });
  }

  /* ─── Why Choose Us Animation ────────────────────────── */
  private animateWhyUs(dur?: number): void {
    // Stat cards entrance
    gsap.set('.stat-card', { autoAlpha: 0, y: 40, scale: 0.9 });
    ScrollTrigger.batch('.stat-card', {
      onEnter: (elements: Element[]) => {
        gsap.to(elements, {
          autoAlpha: 1,
          y: 0,
          scale: 1,
          stagger: 0.1,
          duration: dur ?? 0.6,
          ease: 'back.out(1.4)',
          overwrite: 'auto',
        });
      },
      start: 'top 85%',
      once: true,
    });

    // Counter animation
    const counters = this.el.nativeElement.querySelectorAll(
      '.count-num'
    ) as NodeListOf<HTMLElement>;
    counters.forEach((num: HTMLElement) => {
      const target = parseInt(num.getAttribute('data-target') || '0', 10);
      const obj = { val: 0 };

      gsap.to(obj, {
        val: target,
        duration: dur ?? 2,
        ease: 'power1.out',
        onUpdate: () => {
          num.textContent = Math.floor(obj.val).toLocaleString('vi-VN');
        },
        scrollTrigger: {
          trigger: num,
          start: 'top 85%',
          toggleActions: 'play none none none',
        },
      });
    });

    // Feature cards entrance
    gsap.set('.feature-card', { autoAlpha: 0, y: 40 });
    ScrollTrigger.batch('.feature-card', {
      onEnter: (elements: Element[]) => {
        gsap.to(elements, {
          autoAlpha: 1,
          y: 0,
          stagger: 0.1,
          duration: dur ?? 0.6,
          ease: 'power2.out',
          overwrite: 'auto',
        });
      },
      start: 'top 85%',
      once: true,
    });
  }

  /* ─── 3D Hover Tilt ──────────────────────────────────── */
  private init3DTilt(): void {
    const cards = this.el.nativeElement.querySelectorAll(
      '.benefit-card, .pesticide-card, .advice-card, .stat-card, .feature-card, .campaign-card'
    ) as NodeListOf<HTMLElement>;

    cards.forEach((card: HTMLElement) => {
      card.addEventListener('mousemove', (e: MouseEvent) => {
        const rect = card.getBoundingClientRect();
        const x = (e.clientX - rect.left) / rect.width - 0.5;
        const y = (e.clientY - rect.top) / rect.height - 0.5;

        gsap.to(card, {
          rotationY: x * 12,
          rotationX: -y * 12,
          y: -6,
          transformPerspective: 1000,
          ease: 'power1.out',
          duration: 0.3,
          overwrite: 'auto',
        });
      });

      card.addEventListener('mouseleave', () => {
        gsap.to(card, {
          rotationY: 0,
          rotationX: 0,
          y: 0,
          ease: 'power2.out',
          duration: 0.5,
          overwrite: 'auto',
        });
      });
    });
  }
}


