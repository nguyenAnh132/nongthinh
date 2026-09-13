import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, of, Subject } from 'rxjs';
import { FollowApiService, FollowPage } from '../../core/api/follow-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { FollowPanel } from './follow-panel';

describe('FollowPanel', () => {
  it('loads at 70%, appends pages, retries failures, and resets on tab changes', async () => {
    const person = { userId: 'author', displayName: 'Tác giả', role: 'FARMER', avatarUrl: null,
      followerCount: 21, followingCount: 2, following: false };
    const list = vi.fn((_id: string, _kind: string, page: number): Observable<{ result: FollowPage }> => of({ result: {
      items: Array.from({ length: 20 }, (_, index) => ({ ...person, userId: `member-${page * 20 + index}`, displayName: 'Người theo dõi' })), page, size: 20, hasNext: page === 0,
    } }));
    TestBed.configureTestingModule({ imports: [FollowPanel], providers: [
      provideRouter([]),
      { provide: AuthService, useValue: { currentUser: signal({ userId: 'viewer' }) } },
      { provide: FollowApiService, useValue: { profile: () => of({ result: person }), list,
        revision: signal(0), following: signal({}), pending: signal({}) } },
    ] });
    const fixture = TestBed.createComponent(FollowPanel);
    fixture.componentRef.setInput('userId', 'author');
    fixture.detectChanges();
    await fixture.whenStable();
    const dialog = fixture.nativeElement.querySelector('dialog') as HTMLDialogElement;
    dialog.showModal = vi.fn();
    dialog.close = vi.fn();
    expect(list).not.toHaveBeenCalled();
    fixture.nativeElement.querySelector('.counts button').click();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(list).toHaveBeenLastCalledWith('author', 'followers', 0);
    expect(dialog.showModal).toHaveBeenCalledOnce();
    const members = fixture.nativeElement.querySelector('.members') as HTMLDivElement;
    Object.defineProperties(members, {
      scrollHeight: { value: 2000 },
      clientHeight: { value: 400 },
    });
    members.scrollTop = 1119;
    members.dispatchEvent(new Event('scroll'));
    expect(list).toHaveBeenCalledTimes(1);
    const pending = new Subject<{ result: FollowPage }>();
    list.mockReturnValueOnce(pending);
    members.scrollTop = 1120;
    members.dispatchEvent(new Event('scroll'));
    fixture.detectChanges();
    await fixture.whenStable();
    expect(list).toHaveBeenLastCalledWith('author', 'followers', 1);
    expect(members.querySelectorAll('.member')).toHaveLength(20);
    members.dispatchEvent(new Event('scroll'));
    expect(list).toHaveBeenCalledTimes(2);
    pending.error(new Error('Network unavailable'));
    fixture.detectChanges();
    expect(members.querySelectorAll('.member')).toHaveLength(20);
    members.dispatchEvent(new Event('scroll'));
    expect(list).toHaveBeenCalledTimes(2);
    (members.querySelector('[role="alert"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(list).toHaveBeenLastCalledWith('author', 'followers', 1);
    expect(members.querySelectorAll('.member')).toHaveLength(40);
    members.dispatchEvent(new Event('scroll'));
    expect(list).toHaveBeenCalledTimes(3);
    expect(fixture.nativeElement.querySelector('footer')).toBeNull();
    fixture.nativeElement.querySelectorAll('nav button')[1].click();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(list).toHaveBeenLastCalledWith('author', 'following', 0);
    expect(members.querySelectorAll('.member')).toHaveLength(20);
    expect(members.scrollTop).toBe(0);
    members.scrollTop = 1120;
    members.dispatchEvent(new Event('scroll'));
    fixture.detectChanges();
    await fixture.whenStable();
    expect(list).toHaveBeenLastCalledWith('author', 'following', 1);
    expect(members.querySelectorAll('.member')).toHaveLength(40);
  });
});
