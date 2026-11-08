/**
 * 图片墙的大图预览。
 *
 * 不用任何第三方库：点击图片 → 打开遮罩 → 显示原图。
 * 支持 ESC 关闭、点背景关闭、左右方向键切换上一张/下一张。
 */
(function () {
    'use strict';

    var lightbox = document.getElementById('lightbox');
    if (!lightbox) {
        return;
    }

    var imgEl = document.getElementById('lightbox-img');
    var captionEl = document.getElementById('lightbox-caption');
    var triggers = Array.prototype.slice.call(document.querySelectorAll('.figure-btn'));
    var currentIndex = -1;

    function open(index) {
        var trigger = triggers[index];
        if (!trigger) {
            return;
        }
        currentIndex = index;
        imgEl.src = trigger.getAttribute('data-src');
        var caption = trigger.getAttribute('data-caption') || '';
        captionEl.textContent = caption;
        lightbox.hidden = false;
        document.body.style.overflow = 'hidden';
    }

    function close() {
        lightbox.hidden = true;
        imgEl.src = '';
        document.body.style.overflow = '';
        currentIndex = -1;
    }

    function step(offset) {
        if (currentIndex < 0) {
            return;
        }
        // 首尾循环，浏览时不用反复退出再点
        var next = (currentIndex + offset + triggers.length) % triggers.length;
        open(next);
    }

    triggers.forEach(function (trigger, index) {
        trigger.addEventListener('click', function () {
            open(index);
        });
    });

    lightbox.addEventListener('click', function (event) {
        // 点图片本身不关闭，点遮罩或关闭按钮才关
        if (event.target === lightbox || event.target.classList.contains('lightbox-close')) {
            close();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (lightbox.hidden) {
            return;
        }
        if (event.key === 'Escape') {
            close();
        } else if (event.key === 'ArrowLeft') {
            step(-1);
        } else if (event.key === 'ArrowRight') {
            step(1);
        }
    });
})();
