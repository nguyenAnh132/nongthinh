import { TestBed } from '@angular/core/testing';
import { ConfirmDialogComponent } from './confirm-dialog';

describe('ConfirmDialogComponent', () => {
  it('emits confirmation and dismissal actions', async () => {
    await TestBed.configureTestingModule({ imports: [ConfirmDialogComponent] }).compileComponents();
    const fixture = TestBed.createComponent(ConfirmDialogComponent);
    fixture.componentRef.setInput('title', 'Xóa bài viết?');
    fixture.componentRef.setInput('message', 'Bài viết sẽ bị xóa. Bạn sẽ không thể hoàn tác.');
    fixture.detectChanges();
    const confirmed = vi.fn();
    const dismissed = vi.fn();
    fixture.componentInstance.confirmed.subscribe(confirmed);
    fixture.componentInstance.dismissed.subscribe(dismissed);

    (fixture.nativeElement.querySelector('.confirm-dialog__submit') as HTMLButtonElement).click();
    expect(confirmed).toHaveBeenCalledOnce();

    (
      fixture.nativeElement.querySelector('.confirm-dialog__actions button') as HTMLButtonElement
    ).click();
    expect(dismissed).toHaveBeenCalledOnce();
  });

  it('blocks all actions while busy', async () => {
    await TestBed.configureTestingModule({ imports: [ConfirmDialogComponent] }).compileComponents();
    const fixture = TestBed.createComponent(ConfirmDialogComponent);
    fixture.componentRef.setInput('title', 'Đăng xuất?');
    fixture.componentRef.setInput('message', 'Bạn có chắc chắn muốn đăng xuất?');
    fixture.componentRef.setInput('busy', true);
    fixture.detectChanges();
    const confirmed = vi.fn();
    const dismissed = vi.fn();
    fixture.componentInstance.confirmed.subscribe(confirmed);
    fixture.componentInstance.dismissed.subscribe(dismissed);

    fixture.componentInstance.confirm();
    fixture.componentInstance.dismiss();
    expect(confirmed).not.toHaveBeenCalled();
    expect(dismissed).not.toHaveBeenCalled();
  });
});
