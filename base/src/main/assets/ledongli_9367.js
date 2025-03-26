/*! For license information please see 9367.js.LICENSE.txt */
(self.webpackJsonp = self.webpackJsonp || []).push([
  [9367],
  {
    4566: function (e, t, n) {
      "use strict";
      var i = n(1893),
        r = n(5456),
        a = n(8001),
        o = n(4848),
        s = n(7289),
        l = n(6540),
        c = n(3768),
        d = n(8790),
        u = n(5460),
        p = n(6185);
      t.A = function ActionSheet(e) {
        const {
            round: t = !0,
            zIndex: n,
            overlay: h = !0,
            closeOnClickOverlay: m = !0,
            closeOnClickAction: f = !0,
            safeAreaInsetBottom: v = !0,
            show: g,
            title: w,
            description: b,
            actions: y,
            cancelText: A,
            children: x,
            onSelect: _,
            onCancel: E,
            onClose: S,
            onClickOverlay: C,
            className: T,
          } = e,
          k = (0, a._)(e, [
            "round",
            "zIndex",
            "overlay",
            "closeOnClickOverlay",
            "closeOnClickAction",
            "safeAreaInsetBottom",
            "show",
            "title",
            "description",
            "actions",
            "cancelText",
            "children",
            "onSelect",
            "onCancel",
            "onClose",
            "onClickOverlay",
            "className",
          ]),
          M = (0, l.useCallback)(() => {
            null == E || E();
          }, [E]),
          I = (0, l.useCallback)(() => {
            null == S || S();
          }, [S]),
          L = (0, l.useCallback)(
            (e) => {
              const { index: t } = e.currentTarget.dataset,
                n = null == y ? void 0 : y[t];
              n &&
                (Object.defineProperty(e, "detail", {
                  value: n,
                }),
                null == _ || _(e),
                f && I());
            },
            [I, y, f, _]
          ),
          P = (0, l.useCallback)(() => {
            null == C || C(), null == S || S();
          }, [C, S]);
        return (0, o.jsx)(
          u.A,
          (0, r._)(
            (0, i._)(
              {
                show: g,
                position: "bottom",
                round: t,
                zIndex: n,
                overlay: h,
                className: `van-action-sheet ${T || ""}`,
                safeAreaInsetBottom: v,
                closeOnClickOverlay: m,
                onClose: P,
              },
              k
            ),
            {
              children: (0, o.jsxs)(o.Fragment, {
                children: [
                  w &&
                    (0, o.jsxs)(s.Ss, {
                      className: "van-action-sheet__header",
                      children: [
                        w,
                        (0, o.jsx)(p.A, {
                          name: "cross",
                          className: "van-action-sheet__close",
                          onClick: I,
                        }),
                      ],
                    }),
                  b &&
                    (0, o.jsx)(s.Ss, {
                      className:
                        "van-action-sheet__description van-hairline--bottom",
                      children: b,
                    }),
                  y &&
                    y.length &&
                    (0, o.jsx)(s.Ss, {
                      children: y.map((e, t) => {
                        const {
                            name: n,
                            subname: l,
                            disabled: u,
                            loading: p,
                            openType: h,
                            color: m,
                            className: f,
                          } = e,
                          v = (0, a._)(e, [
                            "name",
                            "subname",
                            "disabled",
                            "loading",
                            "openType",
                            "color",
                            "className",
                          ]);
                        return (0, o.jsx)(
                          s.$n,
                          (0, r._)(
                            (0, i._)(
                              {
                                openType: u || p ? void 0 : h,
                                style: m ? "color: " + m : "",
                                className:
                                  c.Tu("action-sheet__item", {
                                    disabled: u || p,
                                  }) +
                                  " " +
                                  (f || ""),
                                hoverClass: "van-action-sheet__item--hover",
                                "data-index": t,
                                onClick: u || p ? () => {} : L,
                              },
                              v
                            ),
                            {
                              children: p
                                ? (0, o.jsx)(d.A, {
                                    className: "van-action-sheet__loading",
                                    size: "22px",
                                  })
                                : (0, o.jsxs)(o.Fragment, {
                                    children: [
                                      n,
                                      l &&
                                        (0, o.jsx)(s.Ss, {
                                          className:
                                            "van-action-sheet__subname",
                                          children: l,
                                        }),
                                    ],
                                  }),
                            }
                          ),
                          t
                        );
                      }),
                    }),
                  x,
                  A &&
                    (0, o.jsxs)(o.Fragment, {
                      children: [
                        (0, o.jsx)(s.Ss, {
                          className: "van-action-sheet__gap",
                        }),
                        (0, o.jsx)(s.Ss, {
                          className: "van-action-sheet__cancel",
                          hoverClass: "van-action-sheet__cancel--hover",
                          onClick: M,
                          children: A,
                        }),
                      ],
                    }),
                ],
              }),
            }
          )
        );
      };
    },
    8210: function (e, t, n) {
      "use strict";
      n.d(t, {
        KY: function () {
          return getAllRect;
        },
        _t: function () {
          return pickExclude;
        },
        l: function () {
          return getRect;
        },
        xi: function () {
          return requestAnimationFrame;
        },
        y1: function () {
          return range;
        },
      });
      var i = n(1072),
        r = n(765),
        a = n.n(r),
        o = n(93);
      function range(e, t, n) {
        return Math.min(Math.max(e, t), n);
      }
      function requestAnimationFrame(e) {
        return window.requestAnimationFrame
          ? window.requestAnimationFrame(e)
          : a()(e);
      }
      function pickExclude(e, t) {
        return (0, o.Qd)(e)
          ? Object.keys(e).reduce(
              (n, i) => (t.includes(i) || (n[i] = e[i]), n),
              {}
            )
          : {};
      }
      function getRect(e, t, n) {
        const r = t;
        return new Promise((t) => {
          let n = (0, i._Y)();
          e && (n = n.in(e)),
            n
              .select(r)
              .boundingClientRect()
              .exec((e = []) => t(e[0]));
        });
      }
      function getAllRect(e, t, n) {
        const r = t;
        return new Promise((t) => {
          let n = (0, i._Y)();
          e && (n = n.in(e)),
            n
              .selectAll(r)
              .boundingClientRect()
              .exec((e = []) => t(e[0]));
        });
      }
    },
    93: function (e, t, n) {
      "use strict";
      function isFunction(e) {
        return "function" == typeof e;
      }
      function isPlainObject(e) {
        return null !== e && "object" == typeof e && !Array.isArray(e);
      }
      function isPromise(e) {
        return isPlainObject(e) && isFunction(e.then) && isFunction(e.catch);
      }
      function isDef(e) {
        return null != e;
      }
      function isObj(e) {
        const t = typeof e;
        return null !== e && ("object" === t || "function" === t);
      }
      function isBoolean(e) {
        return "boolean" == typeof e;
      }
      n.d(t, {
        C8: function () {
          return isDef;
        },
        Lm: function () {
          return isBoolean;
        },
        Qd: function () {
          return isPlainObject;
        },
        Zj: function () {
          return isImageUrl;
        },
        r1: function () {
          return isVideoUrl;
        },
        sA: function () {
          return isObj;
        },
        yL: function () {
          return isPromise;
        },
      });
      const i = /\.(jpeg|jpg|gif|png|svg|webp|jfif|bmp|dpg)/i,
        r = /\.(mp4|mpg|mpeg|dat|asf|avi|rm|rmvb|mov|wmv|flv|mkv)/i;
      function isImageUrl(e) {
        return i.test(e);
      }
      function isVideoUrl(e) {
        return r.test(e);
      }
    },
    6738: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return u;
        },
      });
      var i = n(1893),
        r = n(5456),
        a = n(8001),
        o = n(4848),
        s = n(7289),
        l = n(3768),
        c = n(3579),
        d = n(5103);
      function mapThemeVarsToCSSVars(e) {
        const t = {};
        return (
          (0, c.H)(e).forEach(function (n) {
            const i =
              "--" +
              (function kebabCase(e) {
                var t;
                return null ===
                  (t = e.replace(/[A-Z]/g, function (e) {
                    return "-" + e;
                  })) || void 0 === t
                  ? void 0
                  : t.toLowerCase().replace(/^-/, "");
              })(n);
            t[i] = e[n];
          }),
          (0, d.i)(t)
        );
      }
      var u = function ConfigProvider(e) {
        const { themeVars: t = {}, children: n, style: c, className: d } = e,
          u = (0, a._)(e, ["themeVars", "children", "style", "className"]);
        return (0, o.jsx)(
          s.Ss,
          (0, r._)(
            (0, i._)(
              {
                className: `van-config-provider ${d || ""}`,
                style: l.iF([mapThemeVarsToCSSVars(t), c]),
              },
              u
            ),
            {
              children: n,
            }
          )
        );
      };
    },
    6185: function (e, t, n) {
      "use strict";
      n.d(t, {
        I: function () {
          return Icon;
        },
        A: function () {
          return p;
        },
      });
      var i = n(1893),
        r = n(5456),
        a = n(8001),
        o = n(4848),
        s = n(7289),
        l = n(3768),
        c = n(3430),
        d = n(5103),
        u = n(4420);
      function isImage(e) {
        return -1 !== e.indexOf("/");
      }
      function rootClass(e) {
        const t = [];
        if ((e.classPrefix && t.push(e.classPrefix), isImage(e.name)))
          t.push("van-icon--image");
        else {
          const n = e.classPrefix ? `${e.classPrefix}-${e.name}` : e.name;
          t.push(n);
        }
        return t.join(" ");
      }
      function rootStyle(e) {
        return (0, d.i)([
          {
            color: e.color,
            "font-size": (0, u._)(e.size),
            height: (0, u._)(e.size),
          },
        ]);
      }
      function Icon(e) {
        const {
            classPrefix: t = "van-icon",
            name: n,
            color: d,
            size: u,
            dot: p,
            info: h,
            style: m,
            className: f,
          } = e,
          v = (0, a._)(e, [
            "classPrefix",
            "name",
            "color",
            "size",
            "dot",
            "info",
            "style",
            "className",
          ]);
        return (0, o.jsxs)(
          s.Ss,
          (0, r._)(
            (0, i._)(
              {
                className:
                  rootClass({
                    classPrefix: t,
                    name: n,
                  }) + ` ${f || ""}`,
                style: l.iF([
                  rootStyle({
                    color: d,
                    size: u,
                  }),
                  m,
                ]),
              },
              v
            ),
            {
              children: [
                (h || 0 === h || p) &&
                  (0, o.jsx)(c.R, {
                    dot: p,
                    info: h,
                    className: "van-icon__info",
                  }),
                isImage(n) &&
                  (0, o.jsx)(s._V, {
                    src: n,
                    mode: "aspectFit",
                    className: "van-icon__image",
                  }),
              ],
            }
          )
        );
      }
      var p = Icon;
    },
    3430: function (e, t, n) {
      "use strict";
      n.d(t, {
        R: function () {
          return Info;
        },
      });
      var i = n(1893),
        r = n(5456),
        a = n(8001),
        o = n(4848),
        s = n(7289),
        l = n(3768);
      function Info(e) {
        const { dot: t, info: n = null, style: c, className: d } = e,
          u = (0, a._)(e, ["dot", "info", "style", "className"]);
        return (0, o.jsx)(o.Fragment, {
          children:
            (n || 0 === n || t) &&
            (0, o.jsx)(
              s.Ss,
              (0, r._)(
                (0, i._)(
                  {
                    className:
                      "van-info " +
                      l.Tu("info", {
                        dot: t,
                      }) +
                      "  " +
                      d,
                    style: l.iF([c]),
                  },
                  u
                ),
                {
                  children: t ? "" : n,
                }
              )
            ),
        });
      }
    },
    8790: function (e, t, n) {
      "use strict";
      n.d(t, {
        R: function () {
          return Loading;
        },
        A: function () {
          return u;
        },
      });
      var i = n(1893),
        r = n(5456),
        a = n(8001),
        o = n(4848),
        s = n(7289),
        l = n(6540),
        c = n(3768),
        d = n(4420);
      function textStyle(e) {
        return (0, c.iF)({
          "font-size": (0, d._)(e.textSize),
        });
      }
      function Loading(e) {
        const {
            vertical: t,
            type: n = "circular",
            color: u,
            size: p,
            textSize: h,
            className: m,
            children: f,
            style: v,
          } = e,
          g = (0, a._)(e, [
            "vertical",
            "type",
            "color",
            "size",
            "textSize",
            "className",
            "children",
            "style",
          ]),
          [w] = (0, l.useState)(
            Array.from({
              length: 12,
            })
          );
        return (0, o.jsxs)(
          s.Ss,
          (0, r._)(
            (0, i._)(
              {
                className:
                  " " +
                  c.Tu("loading", {
                    vertical: t,
                  }) +
                  " " +
                  m,
                style: c.iF([v]),
              },
              g
            ),
            {
              children: [
                (0, o.jsx)(s.Ss, {
                  className: "van-loading__spinner van-loading__spinner--" + n,
                  style:
                    ((b = {
                      color: u,
                      size: p,
                    }),
                    (0, c.iF)({
                      color: b.color,
                      width: (0, d._)(b.size),
                      height: (0, d._)(b.size),
                    })),
                  children:
                    "spinner" === n &&
                    (0, o.jsx)(o.Fragment, {
                      children: w.map((e, t) =>
                        (0, o.jsx)(
                          s.Ss,
                          {
                            className: "van-loading__dot",
                          },
                          `van-loading__dot_${t}`
                        )
                      ),
                    }),
                }),
                (0, o.jsx)(s.Ss, {
                  className: "van-loading__text",
                  style: textStyle({
                    textSize: h,
                  }),
                  children: f,
                }),
              ],
            }
          )
        );
        var b;
      }
      var u = Loading;
    },
    2653: function (e, t, n) {
      "use strict";
      n.d(t, {
        p: function () {
          return useTransition;
        },
      });
      var i = n(6540),
        r = n(93);
      const getClassNames = (e) => ({
        enter: `van-${e}-enter van-${e}-enter-active enter-class enter-active-class`,
        "enter-to": `van-${e}-enter-to van-${e}-enter-active enter-to-class enter-active-class`,
        leave: `van-${e}-leave van-${e}-leave-active leave-class leave-active-class`,
        "leave-to": `van-${e}-leave-to van-${e}-leave-active leave-to-class leave-active-class`,
      });
      function useTransition({
        show: e = !1,
        duration: t = 300,
        name: n = "fade",
        onBeforeEnter: a,
        onBeforeLeave: o,
        onAfterEnter: s,
        onAfterLeave: l,
        onEnter: c,
        onLeave: d,
        enterClass: u,
        enterActiveClass: p,
        enterToClass: h,
        leaveClass: m,
        leaveActiveClass: f,
        leaveToClass: v,
      }) {
        const g = (0, i.useRef)(!1),
          w = (0, i.useRef)(""),
          [b, y] = (0, i.useState)(!1),
          [A, x] = (0, i.useState)(!1),
          [_, E] = (0, i.useState)(0),
          [S, C] = (0, i.useState)(""),
          T = (0, i.useMemo)(() => {
            const e = getClassNames(n);
            return (
              n ||
                ((e.enter += ` ${null != u ? u : ""}`),
                (e["enter-to"] += `${null != h ? h : ""} ${
                  null != p ? p : ""
                } `),
                (e.leave += `  ${null != m ? m : ""}`),
                (e["leave-to"] += ` ${null != v ? v : ""} ${
                  null != f ? f : ""
                }`)),
              e
            );
          }, [p, u, h, f, m, v, n]),
          k = (0, i.useCallback)(() => {
            g.current ||
              ((g.current = !0),
              "enter" === w.current ? null == s || s() : null == l || l(),
              !e && b && y(!1));
          }, [b, s, l, e]),
          M = (0, i.useCallback)(() => {
            const e = (0, r.sA)(t) ? t.enter : t;
            (w.current = "enter"),
              null == a || a(),
              requestAnimationFrame(() => {
                "enter" === w.current &&
                  (null == c || c(),
                  x(!0),
                  y(!0),
                  C(T.enter),
                  E(e),
                  requestAnimationFrame(() => {
                    "enter" === w.current &&
                      ((g.current = !1),
                      setTimeout(() => k(), e),
                      C(T["enter-to"]));
                  }));
              });
          }, [t, a, c, T, k]),
          I = (0, i.useCallback)(() => {
            if (!b) return;
            const e = (0, r.sA)(t) ? t.leave : t;
            (w.current = "leave"),
              null == o || o(),
              requestAnimationFrame(() => {
                "leave" === w.current &&
                  (null == d || d(),
                  C(T.leave),
                  E(e),
                  requestAnimationFrame(() => {
                    "leave" === w.current &&
                      ((g.current = !1),
                      setTimeout(() => k(), e),
                      C(T["leave-to"]));
                  }));
              });
          }, [T, b, t, o, d, k]);
        return (
          (0, i.useEffect)(() => {
            !e || (S && S.includes(T["enter-to"])) || M(), e || I();
          }, [e]),
          {
            display: b,
            inited: A,
            currentDuration: _,
            classes: S,
            onTransitionEnd: k,
          }
        );
      }
    },
    3706: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return g;
        },
      });
      var i = n(1893),
        r = n(5456),
        a = n(8001),
        o = n(4848),
        s = n(5450),
        l = n(4263),
        c = n(6613),
        d = n(6540),
        u = n(7289),
        p = n(3768),
        h = n(8210),
        m = n(6185),
        f = n(5103);
      function rootStyle(e) {
        return (0, f.i)({
          color: e.color,
          "background-color": e.backgroundColor,
          background: e.background,
        });
      }
      let v = 0;
      var g = function NoticeBar(e) {
        const [t, n] = (0, d.useState)({
            ready: !1,
            show: !0,
            animationData: {
              actions: [],
            },
            unitag: 0,
          }),
          f = {
            animation: null,
            resetAnimation: null,
            timer: null,
            wrapWidth: void 0,
            contentWidth: void 0,
            duration: void 0,
          },
          g = (0, d.useRef)(f),
          {
            text: w = "",
            mode: b = "",
            url: y = "",
            openType: A = "navigate",
            delay: x = 1,
            speed: _ = 60,
            scrollable: E = null,
            leftIcon: S = "",
            color: C = "#ed6a0c",
            backgroundColor: T = "#fffbe8",
            background: k,
            wrapable: M,
            renderLeftIcon: I,
            renderRightIcon: L,
            onClick: P,
            onClose: N,
            style: O,
            className: D,
            children: z,
            rectWrapper: j = "",
          } = e,
          B = (0, a._)(e, [
            "text",
            "mode",
            "url",
            "openType",
            "delay",
            "speed",
            "scrollable",
            "leftIcon",
            "color",
            "backgroundColor",
            "background",
            "wrapable",
            "renderLeftIcon",
            "renderRightIcon",
            "onClick",
            "onClose",
            "style",
            "className",
            "children",
            "rectWrapper",
          ]);
        (0, d.useEffect)(() => {
          n((e) =>
            (0, r._)((0, i._)({}, e), {
              unitag: v++,
            })
          );
        }, []),
          (0, s.HC)(() => {
            0;
          }),
          (0, d.useEffect)(() => {
            (g.current.resetAnimation = (0, l.J)({
              duration: 0,
              timingFunction: "linear",
            })),
              n((e) =>
                (0, r._)((0, i._)({}, e), {
                  ready: !0,
                })
              );
          }, []),
          (0, d.useEffect)(
            () => (
              w && t.ready && F(),
              () => {
                g.current.timer && clearTimeout(g.current.timer);
              }
            ),
            [w, _, t.ready]
          );
        const R = (0, d.useCallback)((e = !1) => {
            g.current.timer && clearTimeout(g.current.timer),
              (g.current.timer = null),
              n((t) =>
                (0, r._)((0, i._)({}, t), {
                  animationData: g.current.resetAnimation
                    .translateX(e ? 0 : g.current.wrapWidth)
                    .step()
                    .export(),
                })
              ),
              setTimeout(() => {
                (0, h.xi)(() => {
                  n((e) =>
                    (0, r._)((0, i._)({}, e), {
                      animationData: g.current.animation
                        .translateX(-g.current.contentWidth)
                        .step()
                        .export(),
                    })
                  );
                });
              }, 10),
              (g.current.timer = setTimeout(() => {
                R();
              }, g.current.duration));
          }, []),
          F = (0, d.useCallback)(() => {
            (0, h.xi)(() => {
              Promise.all([
                (0, h.l)(null, `.van-notice-bar__content_${t.unitag}`, j),
                (0, h.l)(null, `.van-notice-bar__wrap_${t.unitag}`, j),
              ]).then((e) => {
                const t = e[0],
                  n = e[1];
                null != t &&
                  null != n &&
                  t.width &&
                  n.width &&
                  !1 !== E &&
                  (0, c.d)(() => {
                    (E || n.width <= t.width) &&
                      ((g.current.wrapWidth = n.width),
                      (g.current.contentWidth = t.width),
                      (g.current.duration = ((n.width + t.width) / _) * 1e3),
                      (g.current.animation = (0, l.J)({
                        duration: g.current.duration,
                        timingFunction: "linear",
                        delay: x,
                      })),
                      R(!0));
                  });
              });
            });
          }, [t.unitag, E, _, x, R]),
          Y = (0, d.useCallback)(
            (e) => {
              "closeable" === b &&
                (g.current.timer && clearTimeout(g.current.timer),
                (g.current.timer = null),
                n((e) =>
                  (0, r._)((0, i._)({}, e), {
                    show: !1,
                  })
                ),
                null == N || N(e));
            },
            [b, N]
          );
        return (
          t.show &&
          (0, o.jsxs)(
            u.Ss,
            (0, r._)(
              (0, i._)(
                {
                  className:
                    p.Tu("notice-bar", {
                      withicon: b,
                      wrapable: M,
                    }) + ` ${D || ""}`,
                  style: p.iF([
                    rootStyle({
                      color: C,
                      backgroundColor: T,
                      background: k,
                    }),
                    O,
                  ]),
                },
                B
              ),
              {
                onClick: P,
                children: [
                  S
                    ? (0, o.jsx)(m.A, {
                        name: S,
                        className: "van-notice-bar__left-icon",
                      })
                    : I,
                  (0, o.jsx)(u.Ss, {
                    className: `van-notice-bar__wrap van-notice-bar__wrap_${t.unitag}`,
                    children: (0, o.jsxs)(u.Ss, {
                      className:
                        `van-notice-bar__content van-notice-bar__content_${t.unitag} ` +
                        (!1 !== E || M ? "" : "van-ellipsis"),
                      animation: t.animationData,
                      children: [w, !w && z],
                    }),
                  }),
                  "closeable" === b
                    ? (0, o.jsx)(m.A, {
                        className: "van-notice-bar__right-icon",
                        name: "cross",
                        onClick: Y,
                      })
                    : "link" === b
                    ? (0, o.jsx)(u.mD, {
                        url: y,
                        openType: A,
                        children: (0, o.jsx)(m.A, {
                          className: "van-notice-bar__right-icon",
                          name: "arrow",
                        }),
                      })
                    : L,
                ],
              }
            )
          )
        );
      };
    },
    3758: function (e, t, n) {
      "use strict";
      var i = n(1893),
        r = n(5456),
        a = n(8001),
        o = n(4848),
        s = n(6540),
        l = n(3768),
        c = n(3843);
      function OverlayInner(e) {
        const {
            show: t,
            zIndex: n,
            style: d,
            className: u,
            lockScroll: p = !0,
            duration: h = 300,
            setOuterShow: m,
            children: f,
          } = e,
          v = (0, a._)(e, [
            "show",
            "zIndex",
            "style",
            "className",
            "lockScroll",
            "duration",
            "setOuterShow",
            "children",
          ]),
          g = (0, s.useCallback)((e) => {
            e.stopPropagation(), e.preventDefault();
          }, []);
        return p
          ? (0, o.jsx)(
              c.A,
              (0, r._)(
                (0, i._)(
                  {
                    show: t,
                    className: `van-overlay  ${u}`,
                    style: l.iF([
                      {
                        "z-index": n,
                      },
                      d,
                    ]),
                    duration: h,
                    onTouchMove: g,
                    onAfterLeave: () => {
                      setTimeout(() => {
                        m(!1);
                      }, 0);
                    },
                  },
                  v
                ),
                {
                  children: f,
                }
              )
            )
          : (0, o.jsx)(
              c.A,
              (0, r._)(
                (0, i._)(
                  {
                    show: t,
                    className: `van-overlay  ${u || ""}`,
                    style: l.iF([
                      {
                        "z-index": n,
                      },
                      d,
                    ]),
                    duration: h,
                    onAfterLeave: () => m(!1),
                  },
                  v
                ),
                {
                  children: f,
                }
              )
            );
      }
      t.A = function Overlay(e) {
        const { show: t } = e,
          [n, r] = (0, s.useState)(!1);
        return (
          (0, s.useEffect)(() => {
            t && r(!0);
          }, [t]),
          (0, o.jsx)(o.Fragment, {
            children: n
              ? (0, o.jsx)(
                  OverlayInner,
                  (0, i._)(
                    {
                      setOuterShow: r,
                    },
                    e
                  )
                )
              : (0, o.jsx)(o.Fragment, {}),
          })
        );
      };
    },
    5460: function (e, t, n) {
      "use strict";
      n.d(t, {
        z: function () {
          return Popup;
        },
        A: function () {
          return h;
        },
      });
      var i = n(1893),
        r = n(5456),
        a = n(8001),
        o = n(4848),
        s = n(7289),
        l = n(6540),
        c = n(3768),
        d = n(6185);
      var u = n(2653),
        p = n(3758);
      function PopupInner(e) {
        const {
            show: t,
            duration: n = 300,
            round: p,
            closeable: h,
            transition: m,
            zIndex: f,
            closeIcon: v = "cross",
            closeIconPosition: g = "top-right",
            position: w = "center",
            safeAreaInsetBottom: b = !0,
            safeAreaInsetTop: y = !1,
            children: A,
            onBeforeEnter: x,
            onBeforeLeave: _,
            onAfterEnter: E,
            onAfterLeave: S,
            onEnter: C,
            onLeave: T,
            onClose: k,
            setOuterShow: M,
            style: I,
            className: L,
          } = e,
          P = (0, a._)(e, [
            "show",
            "duration",
            "round",
            "closeable",
            "transition",
            "zIndex",
            "closeIcon",
            "closeIconPosition",
            "position",
            "safeAreaInsetBottom",
            "safeAreaInsetTop",
            "children",
            "onBeforeEnter",
            "onBeforeLeave",
            "onAfterEnter",
            "onAfterLeave",
            "onEnter",
            "onLeave",
            "onClose",
            "setOuterShow",
            "style",
            "className",
          ]),
          N = (0, l.useCallback)(() => {
            null == S || S(),
              setTimeout(() => {
                null == M || M(!1);
              }, 0);
          }, [S, M]),
          O = (0, l.useCallback)(() => {
            null == k || k();
          }, [k]),
          {
            inited: D,
            currentDuration: z,
            classes: j,
            display: B,
            onTransitionEnd: R,
          } = (0, u.p)({
            show: t,
            duration: "none" === m ? 0 : n,
            name: m || w,
            onBeforeEnter: x,
            onBeforeLeave: _,
            onAfterEnter: E,
            onAfterLeave: N,
            onEnter: C,
            onLeave: T,
          }),
          F = (0, l.useCallback)(
            (e) =>
              e.replace(
                /([A-Z])/g,
                (e, t) => "-" + (null == t ? void 0 : t.toLowerCase())
              ),
            []
          );
        return (0, o.jsx)(o.Fragment, {
          children:
            D &&
            (0, o.jsxs)(
              s.Ss,
              (0, r._)(
                (0, i._)(
                  {
                    className:
                      j +
                      " " +
                      c.Tu("popup", [
                        w,
                        {
                          round: p,
                          safe: b,
                          safeTop: y,
                        },
                      ]) +
                      `  ${L || ""}`,
                    style: c.iF([
                      ((Y = {
                        zIndex: f,
                        currentDuration: z,
                        display: B,
                      }),
                      c.iF([
                        {
                          "z-index": Y.zIndex,
                          "-webkit-transition-duration":
                            Y.currentDuration + "ms",
                          "transition-duration": Y.currentDuration + "ms",
                        },
                        Y.display ? null : "display: none",
                      ])),
                      I,
                    ]),
                    onTransitionEnd: R,
                  },
                  P
                ),
                {
                  children: [
                    A,
                    h &&
                      (0, o.jsx)(d.A, {
                        name: v,
                        className:
                          "close-icon-class van-popup__close-icon van-popup__close-icon--" +
                          F(g),
                        onClick: O,
                      }),
                  ],
                }
              )
            ),
        });
        var Y;
      }
      function Popup(e) {
        const {
            show: t,
            duration: n = 300,
            zIndex: r,
            overlay: a = !0,
            lockScroll: c = !0,
            overlayStyle: d,
            closeOnClickOverlay: u = !0,
            onClickOverlay: h,
            onClose: m,
          } = e,
          [f, v] = (0, l.useState)(!1);
        (0, l.useEffect)(() => {
          t && v(!0);
        }, [t]);
        const g = (0, l.useCallback)(() => {
          null == h || h(), u && (null == m || m());
        }, [u, h, m]);
        return (0, o.jsxs)(s.Ss, {
          children: [
            (0, o.jsx)(s.Ss, {
              children:
                a &&
                (0, o.jsx)(p.A, {
                  show: t,
                  zIndex: r,
                  style: d,
                  duration: n,
                  onClick: g,
                  lockScroll: c,
                }),
            }),
            (0, o.jsx)(s.Ss, {
              children: f
                ? (0, o.jsx)(
                    PopupInner,
                    (0, i._)(
                      {
                        setOuterShow: v,
                      },
                      e
                    )
                  )
                : (0, o.jsx)(o.Fragment, {}),
            }),
          ],
        });
      }
      var h = Popup;
    },
    3843: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return d;
        },
      });
      var i = n(1893),
        r = n(5456),
        a = n(8001),
        o = n(4848),
        s = n(7289),
        l = n(3768);
      var c = n(2653);
      var d = function Transition(e) {
        const {
            onBeforeEnter: t,
            onBeforeLeave: n,
            onAfterEnter: d,
            onAfterLeave: u,
            onEnter: p,
            onLeave: h,
            duration: m,
            name: f,
            show: v,
            children: g,
            style: w,
            className: b,
            enterClass: y,
            enterActiveClass: A,
            enterToClass: x,
            leaveClass: _,
            leaveActiveClass: E,
            leaveToClass: S,
          } = e,
          C = (0, a._)(e, [
            "onBeforeEnter",
            "onBeforeLeave",
            "onAfterEnter",
            "onAfterLeave",
            "onEnter",
            "onLeave",
            "duration",
            "name",
            "show",
            "children",
            "style",
            "className",
            "enterClass",
            "enterActiveClass",
            "enterToClass",
            "leaveClass",
            "leaveActiveClass",
            "leaveToClass",
          ]),
          {
            currentDuration: T,
            classes: k,
            display: M,
          } = (0, c.p)({
            show: v,
            duration: m,
            name: f,
            enterClass: y,
            enterActiveClass: A,
            enterToClass: x,
            leaveClass: _,
            leaveActiveClass: E,
            leaveToClass: S,
            onBeforeEnter: t,
            onBeforeLeave: n,
            onAfterEnter: d,
            onAfterLeave: u,
            onEnter: p,
            onLeave: h,
          });
        return (0, o.jsx)(o.Fragment, {
          children: (0, o.jsx)(
            s.Ss,
            (0, r._)(
              (0, i._)(
                {
                  className: "van-transition " + k + ` ${b || ""}`,
                  style: l.iF([
                    ((I = {
                      currentDuration: T,
                      display: M,
                    }),
                    l.iF([
                      {
                        "-webkit-transition-duration": I.currentDuration + "ms",
                        "transition-duration": I.currentDuration + "ms",
                      },
                      I.display ? null : "display: none",
                      I.style,
                    ])),
                    w,
                  ]),
                },
                C
              ),
              {
                children: g,
              }
            )
          ),
        });
        var I;
      };
    },
    4420: function (e, t, n) {
      "use strict";
      n.d(t, {
        _: function () {
          return addUnit;
        },
      });
      var i = n(4025);
      function addUnit(e) {
        if (null != e) return /^-?\d+(\.\d+)?$/.test("" + e) ? (0, i.n_)(e) : e;
      }
    },
    843: function (e, t, n) {
      "use strict";
      function isArray(e) {
        return e && "[object Array]" === toString.call(e);
      }
      n.d(t, {
        c: function () {
          return isArray;
        },
      });
    },
    3579: function (e, t, n) {
      "use strict";
      n.d(t, {
        H: function () {
          return keys;
        },
      });
      const i = new RegExp('{|}|"', "g");
      function keys(e) {
        return JSON.stringify(e)
          .replace(i, "")
          .split(",")
          .map(function (e) {
            return e.split(":")[0];
          });
      }
    },
    5103: function (e, t, n) {
      "use strict";
      n.d(t, {
        i: function () {
          return style;
        },
      });
      var i = n(843),
        r = n(3579);
      function style(e) {
        return i.c(e)
          ? e
              .filter(function (e) {
                return null != e && "" !== e;
              })
              .map(function (e) {
                return style(e);
              })
              .join(";") || ""
          : "[object Object]" === toString.call(e)
          ? r
              .H(e)
              .filter(function (t) {
                return null != e[t] && "" !== e[t];
              })
              .map(function (t) {
                return [
                  ((n = t),
                  null ===
                    (i = n.replace(new RegExp("[A-Z]", "g"), function (e) {
                      return "-" + e;
                    })) || void 0 === i
                    ? void 0
                    : i.toLowerCase()),
                  [e[t]],
                ].join(":");
                var n, i;
              })
              .join(";") || ""
          : e || "";
      }
    },
    3768: function (e, t, n) {
      "use strict";
      n.d(t, {
        _V: function () {
          return a._;
        },
        Tu: function () {
          return s;
        },
        iF: function () {
          return o.i;
        },
      });
      var i = n(843),
        r = n(3579);
      function traversing(e, t) {
        t &&
          ("string" == typeof t || "number" == typeof t
            ? e.push(t)
            : i.c(t)
            ? t.forEach(function (t) {
                traversing(e, t);
              })
            : "object" == typeof t &&
              r.H(t).forEach(function (n) {
                t[n] && e.push(n);
              }));
      }
      var a = n(4420),
        o = n(5103);
      const s = (function memoize(e) {
        var t = {};
        return function () {
          var n = (function serializer(e) {
            if (
              1 === e.length &&
              (function isPrimitive(e) {
                var t = typeof e;
                return (
                  "boolean" === t ||
                  "number" === t ||
                  "string" === t ||
                  "undefined" === t ||
                  null === e
                );
              })(e[0])
            )
              return e[0];
            for (var t = {}, n = 0; n < e.length; n++) t["key" + n] = e[n];
            return JSON.stringify(t);
          })(arguments);
          return (
            void 0 === t[n] &&
              (t[n] = (function call(e, t) {
                return 2 === t.length
                  ? e(t[0], t[1])
                  : 1 === t.length
                  ? e(t[0])
                  : e();
              })(e, arguments)),
            t[n]
          );
        };
      })(function _bem(e, t) {
        const n = [];
        return (
          traversing(n, t),
          (function join(e, t) {
            return (
              (e = "van-" + e),
              (t = t.map(function (t) {
                return e + "--" + t;
              })).unshift(e),
              t.join(" ")
            );
          })(e, n)
        );
      });
    },
    3802: function (e, t, n) {
      "use strict";
      n.d(t, {
        X: function () {
          return se;
        },
      });
      var i = n(5544),
        r = n(3029),
        a = n(2901),
        o = n(6919),
        s = n(5501),
        l = n(2284),
        c = n(2902),
        d = n(6349),
        u = n(6782);
      function isObject$1(e) {
        return (
          null !== e &&
          "object" === (0, l.A)(e) &&
          "constructor" in e &&
          e.constructor === Object
        );
      }
      function extend$1(e, t) {
        void 0 === e && (e = {}),
          void 0 === t && (t = {}),
          Object.keys(t).forEach(function (n) {
            void 0 === e[n]
              ? (e[n] = t[n])
              : isObject$1(t[n]) &&
                isObject$1(e[n]) &&
                Object.keys(t[n]).length > 0 &&
                extend$1(e[n], t[n]);
          });
      }
      var p = {
        body: {},
        addEventListener: function addEventListener() {},
        removeEventListener: function removeEventListener() {},
        activeElement: {
          blur: function blur() {},
          nodeName: "",
        },
        querySelector: function querySelector() {
          return null;
        },
        querySelectorAll: function querySelectorAll() {
          return [];
        },
        getElementById: function getElementById() {
          return null;
        },
        createEvent: function createEvent() {
          return {
            initEvent: function initEvent() {},
          };
        },
        createElement: function createElement() {
          return {
            children: [],
            childNodes: [],
            style: {},
            setAttribute: function setAttribute() {},
            getElementsByTagName: function getElementsByTagName() {
              return [];
            },
          };
        },
        createElementNS: function createElementNS() {
          return {};
        },
        importNode: function importNode() {
          return null;
        },
        location: {
          hash: "",
          host: "",
          hostname: "",
          href: "",
          origin: "",
          pathname: "",
          protocol: "",
          search: "",
        },
      };
      function getDocument() {
        var e = "undefined" != typeof document ? document : {};
        return extend$1(e, p), e;
      }
      var h = {
        document: p,
        navigator: {
          userAgent: "",
        },
        location: {
          hash: "",
          host: "",
          hostname: "",
          href: "",
          origin: "",
          pathname: "",
          protocol: "",
          search: "",
        },
        history: {
          replaceState: function replaceState() {},
          pushState: function pushState() {},
          go: function go() {},
          back: function back() {},
        },
        CustomEvent: function CustomEvent() {
          return this;
        },
        addEventListener: function addEventListener() {},
        removeEventListener: function removeEventListener() {},
        getComputedStyle: function getComputedStyle() {
          return {
            getPropertyValue: function getPropertyValue() {
              return "";
            },
          };
        },
        Image: function Image() {},
        Date: function Date() {},
        screen: {},
        setTimeout: function setTimeout() {},
        clearTimeout: function clearTimeout() {},
        matchMedia: function matchMedia() {
          return {};
        },
        requestAnimationFrame: function requestAnimationFrame(e) {
          return "undefined" == typeof setTimeout
            ? (e(), null)
            : setTimeout(e, 0);
        },
        cancelAnimationFrame: function cancelAnimationFrame(e) {
          "undefined" != typeof setTimeout && clearTimeout(e);
        },
      };
      function getWindow() {
        var e = "undefined" != typeof window ? window : {};
        return extend$1(e, h), e;
      }
      function _getPrototypeOf(e) {
        return (
          (_getPrototypeOf = Object.setPrototypeOf
            ? Object.getPrototypeOf
            : function _getPrototypeOf(e) {
                return e.__proto__ || Object.getPrototypeOf(e);
              }),
          _getPrototypeOf(e)
        );
      }
      function _setPrototypeOf(e, t) {
        return (
          (_setPrototypeOf =
            Object.setPrototypeOf ||
            function _setPrototypeOf(e, t) {
              return (e.__proto__ = t), e;
            }),
          _setPrototypeOf(e, t)
        );
      }
      function _construct(e, t, n) {
        return (
          (_construct = (function _isNativeReflectConstruct() {
            if ("undefined" == typeof Reflect || !Reflect.construct) return !1;
            if (Reflect.construct.sham) return !1;
            if ("function" == typeof Proxy) return !0;
            try {
              return (
                Date.prototype.toString.call(
                  Reflect.construct(Date, [], function () {})
                ),
                !0
              );
            } catch (e) {
              return !1;
            }
          })()
            ? Reflect.construct
            : function _construct(e, t, n) {
                var i = [null];
                i.push.apply(i, t);
                var r = new (Function.bind.apply(e, i))();
                return n && _setPrototypeOf(r, n.prototype), r;
              }),
          _construct.apply(null, arguments)
        );
      }
      function _wrapNativeSuper(e) {
        var t = "function" == typeof Map ? new Map() : void 0;
        return (
          (_wrapNativeSuper = function _wrapNativeSuper(e) {
            if (
              null === e ||
              !(function _isNativeFunction(e) {
                return (
                  -1 !== Function.toString.call(e).indexOf("[native code]")
                );
              })(e)
            )
              return e;
            if ("function" != typeof e)
              throw new TypeError(
                "Super expression must either be null or a function"
              );
            if (void 0 !== t) {
              if (t.has(e)) return t.get(e);
              t.set(e, Wrapper);
            }
            function Wrapper() {
              return _construct(
                e,
                arguments,
                _getPrototypeOf(this).constructor
              );
            }
            return (
              (Wrapper.prototype = Object.create(e.prototype, {
                constructor: {
                  value: Wrapper,
                  enumerable: !1,
                  writable: !0,
                  configurable: !0,
                },
              })),
              _setPrototypeOf(Wrapper, e)
            );
          }),
          _wrapNativeSuper(e)
        );
      }
      var m = (function (e) {
        function Dom7(t) {
          var n;
          return (
            (function makeReactive(e) {
              var t = e.__proto__;
              Object.defineProperty(e, "__proto__", {
                get: function get() {
                  return t;
                },
                set: function set(e) {
                  t.__proto__ = e;
                },
              });
            })(
              (function _assertThisInitialized(e) {
                if (void 0 === e)
                  throw new ReferenceError(
                    "this hasn't been initialised - super() hasn't been called"
                  );
                return e;
              })((n = e.call.apply(e, [this].concat(t)) || this))
            ),
            n
          );
        }
        return (
          (function _inheritsLoose(e, t) {
            (e.prototype = Object.create(t.prototype)),
              (e.prototype.constructor = e),
              (e.__proto__ = t);
          })(Dom7, e),
          Dom7
        );
      })(_wrapNativeSuper(Array));
      function arrayFlat(e) {
        void 0 === e && (e = []);
        var t = [];
        return (
          e.forEach(function (e) {
            Array.isArray(e) ? t.push.apply(t, arrayFlat(e)) : t.push(e);
          }),
          t
        );
      }
      function arrayFilter(e, t) {
        return Array.prototype.filter.call(e, t);
      }
      function $(e, t) {
        var n = getWindow(),
          i = getDocument(),
          r = [];
        if (!t && e instanceof m) return e;
        if (!e) return new m(r);
        if ("string" == typeof e) {
          var a = e.trim();
          if (a.indexOf("<") >= 0 && a.indexOf(">") >= 0) {
            var o = "div";
            0 === a.indexOf("<li") && (o = "ul"),
              0 === a.indexOf("<tr") && (o = "tbody"),
              (0 !== a.indexOf("<td") && 0 !== a.indexOf("<th")) || (o = "tr"),
              0 === a.indexOf("<tbody") && (o = "table"),
              0 === a.indexOf("<option") && (o = "select");
            var s = i.createElement(o);
            s.innerHTML = a;
            for (var l = 0; l < s.childNodes.length; l += 1)
              r.push(s.childNodes[l]);
          } else
            r = (function qsa(e, t) {
              if ("string" != typeof e) return [e];
              for (
                var n = [], i = t.querySelectorAll(e), r = 0;
                r < i.length;
                r += 1
              )
                n.push(i[r]);
              return n;
            })(e.trim(), t || i);
        } else if (e.nodeType || e === n || e === i) r.push(e);
        else if (Array.isArray(e)) {
          if (e instanceof m) return e;
          r = e;
        }
        return new m(
          (function arrayUnique(e) {
            for (var t = [], n = 0; n < e.length; n += 1)
              -1 === t.indexOf(e[n]) && t.push(e[n]);
            return t;
          })(r)
        );
      }
      $.fn = m.prototype;
      var f,
        v,
        g,
        w = {
          addClass: function addClass() {
            for (var e = arguments.length, t = new Array(e), n = 0; n < e; n++)
              t[n] = arguments[n];
            var i = arrayFlat(
              t.map(function (e) {
                return e.split(" ");
              })
            );
            return (
              this.forEach(function (e) {
                var t;
                (t = e.classList).add.apply(t, i);
              }),
              this
            );
          },
          removeClass: function removeClass() {
            for (var e = arguments.length, t = new Array(e), n = 0; n < e; n++)
              t[n] = arguments[n];
            var i = arrayFlat(
              t.map(function (e) {
                return e.split(" ");
              })
            );
            return (
              this.forEach(function (e) {
                var t;
                (t = e.classList).remove.apply(t, i);
              }),
              this
            );
          },
          hasClass: function hasClass() {
            for (var e = arguments.length, t = new Array(e), n = 0; n < e; n++)
              t[n] = arguments[n];
            var i = arrayFlat(
              t.map(function (e) {
                return e.split(" ");
              })
            );
            return (
              arrayFilter(this, function (e) {
                return (
                  i.filter(function (t) {
                    return e.classList.contains(t);
                  }).length > 0
                );
              }).length > 0
            );
          },
          toggleClass: function toggleClass() {
            for (var e = arguments.length, t = new Array(e), n = 0; n < e; n++)
              t[n] = arguments[n];
            var i = arrayFlat(
              t.map(function (e) {
                return e.split(" ");
              })
            );
            this.forEach(function (e) {
              i.forEach(function (t) {
                e.classList.toggle(t);
              });
            });
          },
          attr: function attr(e, t) {
            if (1 === arguments.length && "string" == typeof e)
              return this[0] ? this[0].getAttribute(e) : void 0;
            for (var n = 0; n < this.length; n += 1)
              if (2 === arguments.length) this[n].setAttribute(e, t);
              else
                for (var i in e)
                  (this[n][i] = e[i]), this[n].setAttribute(i, e[i]);
            return this;
          },
          removeAttr: function removeAttr(e) {
            for (var t = 0; t < this.length; t += 1) this[t].removeAttribute(e);
            return this;
          },
          transform: function transform(e) {
            for (var t = 0; t < this.length; t += 1)
              this[t].style.transform = e;
            return this;
          },
          transition: function transition$1(e) {
            for (var t = 0; t < this.length; t += 1)
              this[t].style.transitionDuration =
                "string" != typeof e ? e + "ms" : e;
            return this;
          },
          on: function on() {
            for (var e = arguments.length, t = new Array(e), n = 0; n < e; n++)
              t[n] = arguments[n];
            var i = t[0],
              r = t[1],
              a = t[2],
              o = t[3];
            function handleLiveEvent(e) {
              var t = e.target;
              if (t) {
                var n = e.target.dom7EventData || [];
                if ((n.indexOf(e) < 0 && n.unshift(e), $(t).is(r)))
                  a.apply(t, n);
                else
                  for (var i = $(t).parents(), o = 0; o < i.length; o += 1)
                    $(i[o]).is(r) && a.apply(i[o], n);
              }
            }
            function handleEvent(e) {
              var t = (e && e.target && e.target.dom7EventData) || [];
              t.indexOf(e) < 0 && t.unshift(e), a.apply(this, t);
            }
            "function" == typeof t[1] &&
              ((i = t[0]), (a = t[1]), (o = t[2]), (r = void 0)),
              o || (o = !1);
            for (var s, l = i.split(" "), c = 0; c < this.length; c += 1) {
              var d = this[c];
              if (r)
                for (s = 0; s < l.length; s += 1) {
                  var u = l[s];
                  d.dom7LiveListeners || (d.dom7LiveListeners = {}),
                    d.dom7LiveListeners[u] || (d.dom7LiveListeners[u] = []),
                    d.dom7LiveListeners[u].push({
                      listener: a,
                      proxyListener: handleLiveEvent,
                    }),
                    d.addEventListener(u, handleLiveEvent, o);
                }
              else
                for (s = 0; s < l.length; s += 1) {
                  var p = l[s];
                  d.dom7Listeners || (d.dom7Listeners = {}),
                    d.dom7Listeners[p] || (d.dom7Listeners[p] = []),
                    d.dom7Listeners[p].push({
                      listener: a,
                      proxyListener: handleEvent,
                    }),
                    d.addEventListener(p, handleEvent, o);
                }
            }
            return this;
          },
          off: function off() {
            for (var e = arguments.length, t = new Array(e), n = 0; n < e; n++)
              t[n] = arguments[n];
            var i = t[0],
              r = t[1],
              a = t[2],
              o = t[3];
            "function" == typeof t[1] &&
              ((i = t[0]), (a = t[1]), (o = t[2]), (r = void 0)),
              o || (o = !1);
            for (var s = i.split(" "), l = 0; l < s.length; l += 1)
              for (var c = s[l], d = 0; d < this.length; d += 1) {
                var u = this[d],
                  p = void 0;
                if (
                  (!r && u.dom7Listeners
                    ? (p = u.dom7Listeners[c])
                    : r && u.dom7LiveListeners && (p = u.dom7LiveListeners[c]),
                  p && p.length)
                )
                  for (var h = p.length - 1; h >= 0; h -= 1) {
                    var m = p[h];
                    (a && m.listener === a) ||
                    (a &&
                      m.listener &&
                      m.listener.dom7proxy &&
                      m.listener.dom7proxy === a)
                      ? (u.removeEventListener(c, m.proxyListener, o),
                        p.splice(h, 1))
                      : a ||
                        (u.removeEventListener(c, m.proxyListener, o),
                        p.splice(h, 1));
                  }
              }
            return this;
          },
          trigger: function trigger() {
            for (
              var e = getWindow(),
                t = arguments.length,
                n = new Array(t),
                i = 0;
              i < t;
              i++
            )
              n[i] = arguments[i];
            for (var r = n[0].split(" "), a = n[1], o = 0; o < r.length; o += 1)
              for (var s = r[o], l = 0; l < this.length; l += 1) {
                var c = this[l];
                if (e.CustomEvent) {
                  var d = new e.CustomEvent(s, {
                    detail: a,
                    bubbles: !0,
                    cancelable: !0,
                  });
                  (c.dom7EventData = n.filter(function (e, t) {
                    return t > 0;
                  })),
                    c.dispatchEvent(d),
                    (c.dom7EventData = []),
                    delete c.dom7EventData;
                }
              }
            return this;
          },
          transitionEnd: function transitionEnd$1(e) {
            var t = this;
            return (
              e &&
                t.on("transitionend", function fireCallBack(n) {
                  n.target === this &&
                    (e.call(this, n), t.off("transitionend", fireCallBack));
                }),
              this
            );
          },
          outerWidth: function outerWidth(e) {
            if (this.length > 0) {
              if (e) {
                var t = this.styles();
                return (
                  this[0].offsetWidth +
                  parseFloat(t.getPropertyValue("margin-right")) +
                  parseFloat(t.getPropertyValue("margin-left"))
                );
              }
              return this[0].offsetWidth;
            }
            return null;
          },
          outerHeight: function outerHeight(e) {
            if (this.length > 0) {
              if (e) {
                var t = this.styles();
                return (
                  this[0].offsetHeight +
                  parseFloat(t.getPropertyValue("margin-top")) +
                  parseFloat(t.getPropertyValue("margin-bottom"))
                );
              }
              return this[0].offsetHeight;
            }
            return null;
          },
          styles: function styles() {
            var e = getWindow();
            return this[0] ? e.getComputedStyle(this[0], null) : {};
          },
          offset: function offset() {
            if (this.length > 0) {
              var e = getWindow(),
                t = getDocument(),
                n = this[0],
                i = n.getBoundingClientRect(),
                r = t.body,
                a = n.clientTop || r.clientTop || 0,
                o = n.clientLeft || r.clientLeft || 0,
                s = n === e ? e.scrollY : n.scrollTop,
                l = n === e ? e.scrollX : n.scrollLeft;
              return {
                top: i.top + s - a,
                left: i.left + l - o,
              };
            }
            return null;
          },
          css: function css(e, t) {
            var n,
              i = getWindow();
            if (1 === arguments.length) {
              if ("string" != typeof e) {
                for (n = 0; n < this.length; n += 1)
                  for (var r in e) this[n].style[r] = e[r];
                return this;
              }
              if (this[0])
                return i.getComputedStyle(this[0], null).getPropertyValue(e);
            }
            if (2 === arguments.length && "string" == typeof e) {
              for (n = 0; n < this.length; n += 1) this[n].style[e] = t;
              return this;
            }
            return this;
          },
          each: function each(e) {
            return e
              ? (this.forEach(function (t, n) {
                  e.apply(t, [t, n]);
                }),
                this)
              : this;
          },
          html: function html(e) {
            if (void 0 === e) return this[0] ? this[0].innerHTML : null;
            for (var t = 0; t < this.length; t += 1) this[t].innerHTML = e;
            return this;
          },
          text: function text(e) {
            if (void 0 === e)
              return this[0] ? this[0].textContent.trim() : null;
            for (var t = 0; t < this.length; t += 1) this[t].textContent = e;
            return this;
          },
          is: function is(e) {
            var t,
              n,
              i = getWindow(),
              r = getDocument(),
              a = this[0];
            if (!a || void 0 === e) return !1;
            if ("string" == typeof e) {
              if (a.matches) return a.matches(e);
              if (a.webkitMatchesSelector) return a.webkitMatchesSelector(e);
              if (a.msMatchesSelector) return a.msMatchesSelector(e);
              for (t = $(e), n = 0; n < t.length; n += 1)
                if (t[n] === a) return !0;
              return !1;
            }
            if (e === r) return a === r;
            if (e === i) return a === i;
            if (e.nodeType || e instanceof m) {
              for (t = e.nodeType ? [e] : e, n = 0; n < t.length; n += 1)
                if (t[n] === a) return !0;
              return !1;
            }
            return !1;
          },
          index: function index() {
            var e,
              t = this[0];
            if (t) {
              for (e = 0; null !== (t = t.previousSibling); )
                1 === t.nodeType && (e += 1);
              return e;
            }
          },
          eq: function eq(e) {
            if (void 0 === e) return this;
            var t = this.length;
            if (e > t - 1) return $([]);
            if (e < 0) {
              var n = t + e;
              return $(n < 0 ? [] : [this[n]]);
            }
            return $([this[e]]);
          },
          append: function append() {
            for (
              var e, t = getDocument(), n = 0;
              n < arguments.length;
              n += 1
            ) {
              e = n < 0 || arguments.length <= n ? void 0 : arguments[n];
              for (var i = 0; i < this.length; i += 1)
                if ("string" == typeof e) {
                  var r = t.createElement("div");
                  for (r.innerHTML = e; r.firstChild; )
                    this[i].appendChild(r.firstChild);
                } else if (e instanceof m)
                  for (var a = 0; a < e.length; a += 1)
                    this[i].appendChild(e[a]);
                else this[i].appendChild(e);
            }
            return this;
          },
          prepend: function prepend(e) {
            var t,
              n,
              i = getDocument();
            for (t = 0; t < this.length; t += 1)
              if ("string" == typeof e) {
                var r = i.createElement("div");
                for (
                  r.innerHTML = e, n = r.childNodes.length - 1;
                  n >= 0;
                  n -= 1
                )
                  this[t].insertBefore(r.childNodes[n], this[t].childNodes[0]);
              } else if (e instanceof m)
                for (n = 0; n < e.length; n += 1)
                  this[t].insertBefore(e[n], this[t].childNodes[0]);
              else this[t].insertBefore(e, this[t].childNodes[0]);
            return this;
          },
          next: function next(e) {
            return this.length > 0
              ? e
                ? this[0].nextElementSibling &&
                  $(this[0].nextElementSibling).is(e)
                  ? $([this[0].nextElementSibling])
                  : $([])
                : this[0].nextElementSibling
                ? $([this[0].nextElementSibling])
                : $([])
              : $([]);
          },
          nextAll: function nextAll(e) {
            var t = [],
              n = this[0];
            if (!n) return $([]);
            for (; n.nextElementSibling; ) {
              var i = n.nextElementSibling;
              e ? $(i).is(e) && t.push(i) : t.push(i), (n = i);
            }
            return $(t);
          },
          prev: function prev(e) {
            if (this.length > 0) {
              var t = this[0];
              return e
                ? t.previousElementSibling && $(t.previousElementSibling).is(e)
                  ? $([t.previousElementSibling])
                  : $([])
                : t.previousElementSibling
                ? $([t.previousElementSibling])
                : $([]);
            }
            return $([]);
          },
          prevAll: function prevAll(e) {
            var t = [],
              n = this[0];
            if (!n) return $([]);
            for (; n.previousElementSibling; ) {
              var i = n.previousElementSibling;
              e ? $(i).is(e) && t.push(i) : t.push(i), (n = i);
            }
            return $(t);
          },
          parent: function parent(e) {
            for (var t = [], n = 0; n < this.length; n += 1)
              null !== this[n].parentNode &&
                (e
                  ? $(this[n].parentNode).is(e) && t.push(this[n].parentNode)
                  : t.push(this[n].parentNode));
            return $(t);
          },
          parents: function parents(e) {
            for (var t = [], n = 0; n < this.length; n += 1)
              for (var i = this[n].parentNode; i; )
                e ? $(i).is(e) && t.push(i) : t.push(i), (i = i.parentNode);
            return $(t);
          },
          closest: function closest(e) {
            var t = this;
            return void 0 === e
              ? $([])
              : (t.is(e) || (t = t.parents(e).eq(0)), t);
          },
          find: function find(e) {
            for (var t = [], n = 0; n < this.length; n += 1)
              for (
                var i = this[n].querySelectorAll(e), r = 0;
                r < i.length;
                r += 1
              )
                t.push(i[r]);
            return $(t);
          },
          children: function children(e) {
            for (var t = [], n = 0; n < this.length; n += 1)
              for (var i = this[n].children, r = 0; r < i.length; r += 1)
                (e && !$(i[r]).is(e)) || t.push(i[r]);
            return $(t);
          },
          filter: function filter(e) {
            return $(arrayFilter(this, e));
          },
          remove: function remove() {
            for (var e = 0; e < this.length; e += 1)
              this[e].parentNode && this[e].parentNode.removeChild(this[e]);
            return this;
          },
        };
      function nextTick(e, t) {
        return void 0 === t && (t = 0), setTimeout(e, t);
      }
      function now() {
        return Date.now();
      }
      function getTranslate(e, t) {
        void 0 === t && (t = "x");
        var n,
          i,
          r,
          a = getWindow(),
          o = (function getComputedStyle$1(e) {
            var t,
              n = getWindow();
            return (
              n.getComputedStyle && (t = n.getComputedStyle(e, null)),
              !t && e.currentStyle && (t = e.currentStyle),
              t || (t = e.style),
              t
            );
          })(e);
        return (
          a.WebKitCSSMatrix
            ? ((i = o.transform || o.webkitTransform).split(",").length > 6 &&
                (i = i
                  .split(", ")
                  .map(function (e) {
                    return e.replace(",", ".");
                  })
                  .join(", ")),
              (r = new a.WebKitCSSMatrix("none" === i ? "" : i)))
            : (n = (r =
                o.MozTransform ||
                o.OTransform ||
                o.MsTransform ||
                o.msTransform ||
                o.transform ||
                o
                  .getPropertyValue("transform")
                  .replace("translate(", "matrix(1, 0, 0, 1,"))
                .toString()
                .split(",")),
          "x" === t &&
            (i = a.WebKitCSSMatrix
              ? r.m41
              : 16 === n.length
              ? parseFloat(n[12])
              : parseFloat(n[4])),
          "y" === t &&
            (i = a.WebKitCSSMatrix
              ? r.m42
              : 16 === n.length
              ? parseFloat(n[13])
              : parseFloat(n[5])),
          i || 0
        );
      }
      function isObject(e) {
        return (
          "object" === (0, l.A)(e) &&
          null !== e &&
          e.constructor &&
          "Object" === Object.prototype.toString.call(e).slice(8, -1)
        );
      }
      function extend() {
        for (
          var e,
            t = Object(arguments.length <= 0 ? void 0 : arguments[0]),
            n = ["__proto__", "constructor", "prototype"],
            i = 1;
          i < arguments.length;
          i += 1
        ) {
          var r = i < 0 || arguments.length <= i ? void 0 : arguments[i];
          if (
            null != r &&
            ((e = r),
            !("undefined" != typeof window
              ? e instanceof HTMLElement
              : e && (1 === e.nodeType || 11 === e.nodeType)))
          )
            for (
              var a = Object.keys(Object(r)).filter(function (e) {
                  return n.indexOf(e) < 0;
                }),
                o = 0,
                s = a.length;
              o < s;
              o += 1
            ) {
              var l = a[o],
                c = Object.getOwnPropertyDescriptor(r, l);
              void 0 !== c &&
                c.enumerable &&
                (isObject(t[l]) && isObject(r[l])
                  ? r[l].__swiper__
                    ? (t[l] = r[l])
                    : extend(t[l], r[l])
                  : !isObject(t[l]) && isObject(r[l])
                  ? ((t[l] = {}),
                    r[l].__swiper__ ? (t[l] = r[l]) : extend(t[l], r[l]))
                  : (t[l] = r[l]));
            }
        }
        return t;
      }
      function bindModuleMethods(e, t) {
        Object.keys(t).forEach(function (n) {
          isObject(t[n]) &&
            Object.keys(t[n]).forEach(function (i) {
              "function" == typeof t[n][i] && (t[n][i] = t[n][i].bind(e));
            }),
            (e[n] = t[n]);
        });
      }
      function classesToSelector(e) {
        return (
          void 0 === e && (e = ""),
          "." +
            e
              .trim()
              .replace(/([\.:\/])/g, "\\$1")
              .replace(/ /g, ".")
        );
      }
      function createElementIfNotDefined(e, t, n, i) {
        var r = getDocument();
        return (
          n &&
            Object.keys(i).forEach(function (n) {
              if (!t[n] && !0 === t.auto) {
                var a = r.createElement("div");
                (a.className = i[n]), e.append(a), (t[n] = a);
              }
            }),
          t
        );
      }
      function getSupport() {
        return (
          f ||
            (f = (function calcSupport() {
              var e = getWindow(),
                t = getDocument();
              return {
                touch: !!(
                  "ontouchstart" in e ||
                  (e.DocumentTouch && t instanceof e.DocumentTouch)
                ),
                pointerEvents:
                  !!e.PointerEvent &&
                  "maxTouchPoints" in e.navigator &&
                  e.navigator.maxTouchPoints >= 0,
                observer: (function checkObserver() {
                  return (
                    "MutationObserver" in e || "WebkitMutationObserver" in e
                  );
                })(),
                passiveListener: (function checkPassiveListener() {
                  var t = !1;
                  try {
                    var n = Object.defineProperty({}, "passive", {
                      get: function get() {
                        t = !0;
                      },
                    });
                    e.addEventListener("testPassiveListener", null, n);
                  } catch (e) {}
                  return t;
                })(),
                gestures: (function checkGestures() {
                  return "ongesturestart" in e;
                })(),
              };
            })()),
          f
        );
      }
      function getDevice(e) {
        return (
          void 0 === e && (e = {}),
          v ||
            (v = (function calcDevice(e) {
              var t = (void 0 === e ? {} : e).userAgent,
                n = getSupport(),
                i = getWindow(),
                r = i.navigator.platform,
                a = t || i.navigator.userAgent,
                o = {
                  ios: !1,
                  android: !1,
                },
                s = i.screen.width,
                l = i.screen.height,
                c = a.match(/(Android);?[\s\/]+([\d.]+)?/),
                d = a.match(/(iPad).*OS\s([\d_]+)/),
                u = a.match(/(iPod)(.*OS\s([\d_]+))?/),
                p = !d && a.match(/(iPhone\sOS|iOS)\s([\d_]+)/),
                h = "Win32" === r,
                m = "MacIntel" === r;
              return (
                !d &&
                  m &&
                  n.touch &&
                  [
                    "1024x1366",
                    "1366x1024",
                    "834x1194",
                    "1194x834",
                    "834x1112",
                    "1112x834",
                    "768x1024",
                    "1024x768",
                    "820x1180",
                    "1180x820",
                    "810x1080",
                    "1080x810",
                  ].indexOf(s + "x" + l) >= 0 &&
                  ((d = a.match(/(Version)\/([\d.]+)/)) ||
                    (d = [0, 1, "13_0_0"]),
                  (m = !1)),
                c && !h && ((o.os = "android"), (o.android = !0)),
                (d || p || u) && ((o.os = "ios"), (o.ios = !0)),
                o
              );
            })(e)),
          v
        );
      }
      function getBrowser() {
        return (
          g ||
            (g = (function calcBrowser() {
              var e = getWindow();
              return {
                isEdge: !!e.navigator.userAgent.match(/Edge/g),
                isSafari: (function isSafari() {
                  var t = e.navigator.userAgent.toLowerCase();
                  return (
                    t.indexOf("safari") >= 0 &&
                    t.indexOf("chrome") < 0 &&
                    t.indexOf("android") < 0
                  );
                })(),
                isWebView: /(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)/i.test(
                  e.navigator.userAgent
                ),
              };
            })()),
          g
        );
      }
      Object.keys(w).forEach(function (e) {
        Object.defineProperty($.fn, e, {
          value: w[e],
          writable: !0,
        });
      });
      var b = {
        name: "resize",
        create: function create() {
          var e = this;
          extend(e, {
            resize: {
              observer: null,
              createObserver: function createObserver() {
                e &&
                  !e.destroyed &&
                  e.initialized &&
                  ((e.resize.observer = new ResizeObserver(function (t) {
                    var n = e.width,
                      i = e.height,
                      r = n,
                      a = i;
                    t.forEach(function (t) {
                      var n = t.contentBoxSize,
                        i = t.contentRect,
                        o = t.target;
                      (o && o !== e.el) ||
                        ((r = i ? i.width : (n[0] || n).inlineSize),
                        (a = i ? i.height : (n[0] || n).blockSize));
                    }),
                      (r === n && a === i) || e.resize.resizeHandler();
                  })),
                  e.resize.observer.observe(e.el));
              },
              removeObserver: function removeObserver() {
                e.resize.observer &&
                  e.resize.observer.unobserve &&
                  e.el &&
                  (e.resize.observer.unobserve(e.el),
                  (e.resize.observer = null));
              },
              resizeHandler: function resizeHandler() {
                e &&
                  !e.destroyed &&
                  e.initialized &&
                  (e.emit("beforeResize"), e.emit("resize"));
              },
              orientationChangeHandler: function orientationChangeHandler() {
                e &&
                  !e.destroyed &&
                  e.initialized &&
                  e.emit("orientationchange");
              },
            },
          });
        },
        on: {
          init: function init(e) {
            var t = getWindow();
            e.params.resizeObserver &&
            (function supportsResizeObserver() {
              return void 0 !== getWindow().ResizeObserver;
            })()
              ? e.resize.createObserver()
              : (t.addEventListener("resize", e.resize.resizeHandler),
                t.addEventListener(
                  "orientationchange",
                  e.resize.orientationChangeHandler
                ));
          },
          destroy: function destroy(e) {
            var t = getWindow();
            e.resize.removeObserver(),
              t.removeEventListener("resize", e.resize.resizeHandler),
              t.removeEventListener(
                "orientationchange",
                e.resize.orientationChangeHandler
              );
          },
        },
      };
      function _extends$i() {
        return (
          (_extends$i =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$i.apply(this, arguments)
        );
      }
      var y = {
          attach: function attach(e, t) {
            void 0 === t && (t = {});
            var n = getWindow(),
              i = this,
              r = new (n.MutationObserver || n.WebkitMutationObserver)(
                function (e) {
                  if (1 !== e.length) {
                    var t = function observerUpdate() {
                      i.emit("observerUpdate", e[0]);
                    };
                    n.requestAnimationFrame
                      ? n.requestAnimationFrame(t)
                      : n.setTimeout(t, 0);
                  } else i.emit("observerUpdate", e[0]);
                }
              );
            r.observe(e, {
              attributes: void 0 === t.attributes || t.attributes,
              childList: void 0 === t.childList || t.childList,
              characterData: void 0 === t.characterData || t.characterData,
            }),
              i.observer.observers.push(r);
          },
          init: function init() {
            var e = this;
            if (e.support.observer && e.params.observer) {
              if (e.params.observeParents)
                for (var t = e.$el.parents(), n = 0; n < t.length; n += 1)
                  e.observer.attach(t[n]);
              e.observer.attach(e.$el[0], {
                childList: e.params.observeSlideChildren,
              }),
                e.observer.attach(e.$wrapperEl[0], {
                  attributes: !1,
                });
            }
          },
          destroy: function destroy() {
            this.observer.observers.forEach(function (e) {
              e.disconnect();
            }),
              (this.observer.observers = []);
          },
        },
        A = {
          name: "observer",
          params: {
            observer: !1,
            observeParents: !1,
            observeSlideChildren: !1,
          },
          create: function create() {
            bindModuleMethods(this, {
              observer: _extends$i({}, y, {
                observers: [],
              }),
            });
          },
          on: {
            init: function init(e) {
              e.observer.init();
            },
            destroy: function destroy(e) {
              e.observer.destroy();
            },
          },
        },
        x = {
          on: function on(e, t, n) {
            var i = this;
            if ("function" != typeof t) return i;
            var r = n ? "unshift" : "push";
            return (
              e.split(" ").forEach(function (e) {
                i.eventsListeners[e] || (i.eventsListeners[e] = []),
                  i.eventsListeners[e][r](t);
              }),
              i
            );
          },
          once: function once(e, t, n) {
            var i = this;
            if ("function" != typeof t) return i;
            function onceHandler() {
              i.off(e, onceHandler),
                onceHandler.__emitterProxy && delete onceHandler.__emitterProxy;
              for (
                var n = arguments.length, r = new Array(n), a = 0;
                a < n;
                a++
              )
                r[a] = arguments[a];
              t.apply(i, r);
            }
            return (onceHandler.__emitterProxy = t), i.on(e, onceHandler, n);
          },
          onAny: function onAny(e, t) {
            var n = this;
            if ("function" != typeof e) return n;
            var i = t ? "unshift" : "push";
            return (
              n.eventsAnyListeners.indexOf(e) < 0 && n.eventsAnyListeners[i](e),
              n
            );
          },
          offAny: function offAny(e) {
            var t = this;
            if (!t.eventsAnyListeners) return t;
            var n = t.eventsAnyListeners.indexOf(e);
            return n >= 0 && t.eventsAnyListeners.splice(n, 1), t;
          },
          off: function off(e, t) {
            var n = this;
            return n.eventsListeners
              ? (e.split(" ").forEach(function (e) {
                  void 0 === t
                    ? (n.eventsListeners[e] = [])
                    : n.eventsListeners[e] &&
                      n.eventsListeners[e].forEach(function (i, r) {
                        (i === t ||
                          (i.__emitterProxy && i.__emitterProxy === t)) &&
                          n.eventsListeners[e].splice(r, 1);
                      });
                }),
                n)
              : n;
          },
          emit: function emit() {
            var e,
              t,
              n,
              i = this;
            if (!i.eventsListeners) return i;
            for (var r = arguments.length, a = new Array(r), o = 0; o < r; o++)
              a[o] = arguments[o];
            return (
              "string" == typeof a[0] || Array.isArray(a[0])
                ? ((e = a[0]), (t = a.slice(1, a.length)), (n = i))
                : ((e = a[0].events), (t = a[0].data), (n = a[0].context || i)),
              t.unshift(n),
              (Array.isArray(e) ? e : e.split(" ")).forEach(function (e) {
                i.eventsAnyListeners &&
                  i.eventsAnyListeners.length &&
                  i.eventsAnyListeners.forEach(function (i) {
                    i.apply(n, [e].concat(t));
                  }),
                  i.eventsListeners &&
                    i.eventsListeners[e] &&
                    i.eventsListeners[e].forEach(function (e) {
                      e.apply(n, t);
                    });
              }),
              i
            );
          },
        };
      var _ = {
        updateSize: function updateSize() {
          var e,
            t,
            n = this,
            i = n.$el;
          (e =
            void 0 !== n.params.width && null !== n.params.width
              ? n.params.width
              : i[0].clientWidth),
            (t =
              void 0 !== n.params.height && null !== n.params.height
                ? n.params.height
                : i[0].clientHeight),
            (0 === e && n.isHorizontal()) ||
              (0 === t && n.isVertical()) ||
              ((e =
                e -
                parseInt(i.css("padding-left") || 0, 10) -
                parseInt(i.css("padding-right") || 0, 10)),
              (t =
                t -
                parseInt(i.css("padding-top") || 0, 10) -
                parseInt(i.css("padding-bottom") || 0, 10)),
              Number.isNaN(e) && (e = 0),
              Number.isNaN(t) && (t = 0),
              extend(n, {
                width: e,
                height: t,
                size: n.isHorizontal() ? e : t,
              }));
        },
        updateSlides: function updateSlides() {
          var e = this;
          function getDirectionLabel(t) {
            return e.isHorizontal()
              ? t
              : {
                  width: "height",
                  "margin-top": "margin-left",
                  "margin-bottom ": "margin-right",
                  "margin-left": "margin-top",
                  "margin-right": "margin-bottom",
                  "padding-left": "padding-top",
                  "padding-right": "padding-bottom",
                  marginRight: "marginBottom",
                }[t];
          }
          function getDirectionPropertyValue(e, t) {
            return parseFloat(e.getPropertyValue(getDirectionLabel(t)) || 0);
          }
          var t = e.params,
            n = e.$wrapperEl,
            i = e.size,
            r = e.rtlTranslate,
            a = e.wrongRTL,
            o = e.virtual && t.virtual.enabled,
            s = o ? e.virtual.slides.length : e.slides.length,
            l = n.children("." + e.params.slideClass),
            c = o ? e.virtual.slides.length : l.length,
            d = [],
            u = [],
            p = [],
            h = t.slidesOffsetBefore;
          "function" == typeof h && (h = t.slidesOffsetBefore.call(e));
          var m = t.slidesOffsetAfter;
          "function" == typeof m && (m = t.slidesOffsetAfter.call(e));
          var f = e.snapGrid.length,
            v = e.slidesGrid.length,
            g = t.spaceBetween,
            w = -h,
            b = 0,
            y = 0;
          if (void 0 !== i) {
            var A, x;
            "string" == typeof g &&
              g.indexOf("%") >= 0 &&
              (g = (parseFloat(g.replace("%", "")) / 100) * i),
              (e.virtualSize = -g),
              r
                ? l.css({
                    marginLeft: "",
                    marginTop: "",
                  })
                : l.css({
                    marginRight: "",
                    marginBottom: "",
                  }),
              t.slidesPerColumn > 1 &&
                ((A =
                  Math.floor(c / t.slidesPerColumn) ===
                  c / e.params.slidesPerColumn
                    ? c
                    : Math.ceil(c / t.slidesPerColumn) * t.slidesPerColumn),
                "auto" !== t.slidesPerView &&
                  "row" === t.slidesPerColumnFill &&
                  (A = Math.max(A, t.slidesPerView * t.slidesPerColumn)));
            for (
              var _,
                E,
                S,
                C = t.slidesPerColumn,
                T = A / C,
                k = Math.floor(c / t.slidesPerColumn),
                M = 0;
              M < c;
              M += 1
            ) {
              x = 0;
              var I = l.eq(M);
              if (t.slidesPerColumn > 1) {
                var L = void 0,
                  P = void 0,
                  N = void 0;
                if ("row" === t.slidesPerColumnFill && t.slidesPerGroup > 1) {
                  var O = Math.floor(
                      M / (t.slidesPerGroup * t.slidesPerColumn)
                    ),
                    D = M - t.slidesPerColumn * t.slidesPerGroup * O,
                    z =
                      0 === O
                        ? t.slidesPerGroup
                        : Math.min(
                            Math.ceil((c - O * C * t.slidesPerGroup) / C),
                            t.slidesPerGroup
                          );
                  (L =
                    (P =
                      D - (N = Math.floor(D / z)) * z + O * t.slidesPerGroup) +
                    (N * A) / C),
                    I.css({
                      "-webkit-box-ordinal-group": L,
                      "-moz-box-ordinal-group": L,
                      "-ms-flex-order": L,
                      "-webkit-order": L,
                      order: L,
                    });
                } else
                  "column" === t.slidesPerColumnFill
                    ? ((N = M - (P = Math.floor(M / C)) * C),
                      (P > k || (P === k && N === C - 1)) &&
                        (N += 1) >= C &&
                        ((N = 0), (P += 1)))
                    : (P = M - (N = Math.floor(M / T)) * T);
                I.css(
                  getDirectionLabel("margin-top"),
                  0 !== N ? t.spaceBetween && t.spaceBetween + "px" : ""
                );
              }
              if ("none" !== I.css("display")) {
                if ("auto" === t.slidesPerView) {
                  var j = getComputedStyle(I[0]),
                    B = I[0].style.transform,
                    R = I[0].style.webkitTransform;
                  if (
                    (B && (I[0].style.transform = "none"),
                    R && (I[0].style.webkitTransform = "none"),
                    t.roundLengths)
                  )
                    x = e.isHorizontal() ? I.outerWidth(!0) : I.outerHeight(!0);
                  else {
                    var F = getDirectionPropertyValue(j, "width"),
                      Y = getDirectionPropertyValue(j, "padding-left"),
                      G = getDirectionPropertyValue(j, "padding-right"),
                      V = getDirectionPropertyValue(j, "margin-left"),
                      W = getDirectionPropertyValue(j, "margin-right"),
                      H = j.getPropertyValue("box-sizing");
                    if (H && "border-box" === H) x = F + V + W;
                    else {
                      var X = I[0],
                        U = X.clientWidth;
                      x = F + Y + G + V + W + (X.offsetWidth - U);
                    }
                  }
                  B && (I[0].style.transform = B),
                    R && (I[0].style.webkitTransform = R),
                    t.roundLengths && (x = Math.floor(x));
                } else
                  (x = (i - (t.slidesPerView - 1) * g) / t.slidesPerView),
                    t.roundLengths && (x = Math.floor(x)),
                    l[M] && (l[M].style[getDirectionLabel("width")] = x + "px");
                l[M] && (l[M].swiperSlideSize = x),
                  p.push(x),
                  t.centeredSlides
                    ? ((w = w + x / 2 + b / 2 + g),
                      0 === b && 0 !== M && (w = w - i / 2 - g),
                      0 === M && (w = w - i / 2 - g),
                      Math.abs(w) < 0.001 && (w = 0),
                      t.roundLengths && (w = Math.floor(w)),
                      y % t.slidesPerGroup == 0 && d.push(w),
                      u.push(w))
                    : (t.roundLengths && (w = Math.floor(w)),
                      (y - Math.min(e.params.slidesPerGroupSkip, y)) %
                        e.params.slidesPerGroup ==
                        0 && d.push(w),
                      u.push(w),
                      (w = w + x + g)),
                  (e.virtualSize += x + g),
                  (b = x),
                  (y += 1);
              }
            }
            if (
              ((e.virtualSize = Math.max(e.virtualSize, i) + m),
              r &&
                a &&
                ("slide" === t.effect || "coverflow" === t.effect) &&
                n.css({
                  width: e.virtualSize + t.spaceBetween + "px",
                }),
              t.setWrapperSize)
            )
              n.css(
                (((E = {})[getDirectionLabel("width")] =
                  e.virtualSize + t.spaceBetween + "px"),
                E)
              );
            if (t.slidesPerColumn > 1)
              if (
                ((e.virtualSize = (x + t.spaceBetween) * A),
                (e.virtualSize =
                  Math.ceil(e.virtualSize / t.slidesPerColumn) -
                  t.spaceBetween),
                n.css(
                  (((S = {})[getDirectionLabel("width")] =
                    e.virtualSize + t.spaceBetween + "px"),
                  S)
                ),
                t.centeredSlides)
              ) {
                _ = [];
                for (var Z = 0; Z < d.length; Z += 1) {
                  var Q = d[Z];
                  t.roundLengths && (Q = Math.floor(Q)),
                    d[Z] < e.virtualSize + d[0] && _.push(Q);
                }
                d = _;
              }
            if (!t.centeredSlides) {
              _ = [];
              for (var q = 0; q < d.length; q += 1) {
                var J = d[q];
                t.roundLengths && (J = Math.floor(J)),
                  d[q] <= e.virtualSize - i && _.push(J);
              }
              (d = _),
                Math.floor(e.virtualSize - i) - Math.floor(d[d.length - 1]) >
                  1 && d.push(e.virtualSize - i);
            }
            if ((0 === d.length && (d = [0]), 0 !== t.spaceBetween)) {
              var K,
                ee =
                  e.isHorizontal() && r
                    ? "marginLeft"
                    : getDirectionLabel("marginRight");
              l.filter(function (e, n) {
                return !t.cssMode || n !== l.length - 1;
              }).css((((K = {})[ee] = g + "px"), K));
            }
            if (t.centeredSlides && t.centeredSlidesBounds) {
              var te = 0;
              p.forEach(function (e) {
                te += e + (t.spaceBetween ? t.spaceBetween : 0);
              });
              var ne = (te -= t.spaceBetween) - i;
              d = d.map(function (e) {
                return e < 0 ? -h : e > ne ? ne + m : e;
              });
            }
            if (t.centerInsufficientSlides) {
              var ie = 0;
              if (
                (p.forEach(function (e) {
                  ie += e + (t.spaceBetween ? t.spaceBetween : 0);
                }),
                (ie -= t.spaceBetween) < i)
              ) {
                var re = (i - ie) / 2;
                d.forEach(function (e, t) {
                  d[t] = e - re;
                }),
                  u.forEach(function (e, t) {
                    u[t] = e + re;
                  });
              }
            }
            extend(e, {
              slides: l,
              snapGrid: d,
              slidesGrid: u,
              slidesSizesGrid: p,
            }),
              c !== s && e.emit("slidesLengthChange"),
              d.length !== f &&
                (e.params.watchOverflow && e.checkOverflow(),
                e.emit("snapGridLengthChange")),
              u.length !== v && e.emit("slidesGridLengthChange"),
              (t.watchSlidesProgress || t.watchSlidesVisibility) &&
                e.updateSlidesOffset();
          }
        },
        updateAutoHeight: function updateAutoHeight(e) {
          var t,
            n = this,
            i = [],
            r = n.virtual && n.params.virtual.enabled,
            a = 0;
          "number" == typeof e
            ? n.setTransition(e)
            : !0 === e && n.setTransition(n.params.speed);
          var o = function getSlideByIndex(e) {
            return r
              ? n.slides.filter(function (t) {
                  return (
                    parseInt(t.getAttribute("data-swiper-slide-index"), 10) ===
                    e
                  );
                })[0]
              : n.slides.eq(e)[0];
          };
          if ("auto" !== n.params.slidesPerView && n.params.slidesPerView > 1)
            if (n.params.centeredSlides)
              n.visibleSlides.each(function (e) {
                i.push(e);
              });
            else
              for (t = 0; t < Math.ceil(n.params.slidesPerView); t += 1) {
                var s = n.activeIndex + t;
                if (s > n.slides.length && !r) break;
                i.push(o(s));
              }
          else i.push(o(n.activeIndex));
          for (t = 0; t < i.length; t += 1)
            if (void 0 !== i[t]) {
              var l = i[t].offsetHeight;
              a = l > a ? l : a;
            }
          a && n.$wrapperEl.css("height", a + "px");
        },
        updateSlidesOffset: function updateSlidesOffset() {
          for (var e = this.slides, t = 0; t < e.length; t += 1)
            e[t].swiperSlideOffset = this.isHorizontal()
              ? e[t].offsetLeft
              : e[t].offsetTop;
        },
        updateSlidesProgress: function updateSlidesProgress(e) {
          void 0 === e && (e = (this && this.translate) || 0);
          var t = this,
            n = t.params,
            i = t.slides,
            r = t.rtlTranslate;
          if (0 !== i.length) {
            void 0 === i[0].swiperSlideOffset && t.updateSlidesOffset();
            var a = -e;
            r && (a = e),
              i.removeClass(n.slideVisibleClass),
              (t.visibleSlidesIndexes = []),
              (t.visibleSlides = []);
            for (var o = 0; o < i.length; o += 1) {
              var s = i[o],
                l =
                  (a +
                    (n.centeredSlides ? t.minTranslate() : 0) -
                    s.swiperSlideOffset) /
                  (s.swiperSlideSize + n.spaceBetween);
              if (
                n.watchSlidesVisibility ||
                (n.centeredSlides && n.autoHeight)
              ) {
                var c = -(a - s.swiperSlideOffset),
                  d = c + t.slidesSizesGrid[o];
                ((c >= 0 && c < t.size - 1) ||
                  (d > 1 && d <= t.size) ||
                  (c <= 0 && d >= t.size)) &&
                  (t.visibleSlides.push(s),
                  t.visibleSlidesIndexes.push(o),
                  i.eq(o).addClass(n.slideVisibleClass));
              }
              s.progress = r ? -l : l;
            }
            t.visibleSlides = $(t.visibleSlides);
          }
        },
        updateProgress: function updateProgress(e) {
          var t = this;
          if (void 0 === e) {
            var n = t.rtlTranslate ? -1 : 1;
            e = (t && t.translate && t.translate * n) || 0;
          }
          var i = t.params,
            r = t.maxTranslate() - t.minTranslate(),
            a = t.progress,
            o = t.isBeginning,
            s = t.isEnd,
            l = o,
            c = s;
          0 === r
            ? ((a = 0), (o = !0), (s = !0))
            : ((o = (a = (e - t.minTranslate()) / r) <= 0), (s = a >= 1)),
            extend(t, {
              progress: a,
              isBeginning: o,
              isEnd: s,
            }),
            (i.watchSlidesProgress ||
              i.watchSlidesVisibility ||
              (i.centeredSlides && i.autoHeight)) &&
              t.updateSlidesProgress(e),
            o && !l && t.emit("reachBeginning toEdge"),
            s && !c && t.emit("reachEnd toEdge"),
            ((l && !o) || (c && !s)) && t.emit("fromEdge"),
            t.emit("progress", a);
        },
        updateSlidesClasses: function updateSlidesClasses() {
          var e,
            t = this,
            n = t.slides,
            i = t.params,
            r = t.$wrapperEl,
            a = t.activeIndex,
            o = t.realIndex,
            s = t.virtual && i.virtual.enabled;
          n.removeClass(
            i.slideActiveClass +
              " " +
              i.slideNextClass +
              " " +
              i.slidePrevClass +
              " " +
              i.slideDuplicateActiveClass +
              " " +
              i.slideDuplicateNextClass +
              " " +
              i.slideDuplicatePrevClass
          ),
            (e = s
              ? t.$wrapperEl.find(
                  "." + i.slideClass + '[data-swiper-slide-index="' + a + '"]'
                )
              : n.eq(a)).addClass(i.slideActiveClass),
            i.loop &&
              (e.hasClass(i.slideDuplicateClass)
                ? r
                    .children(
                      "." +
                        i.slideClass +
                        ":not(." +
                        i.slideDuplicateClass +
                        ')[data-swiper-slide-index="' +
                        o +
                        '"]'
                    )
                    .addClass(i.slideDuplicateActiveClass)
                : r
                    .children(
                      "." +
                        i.slideClass +
                        "." +
                        i.slideDuplicateClass +
                        '[data-swiper-slide-index="' +
                        o +
                        '"]'
                    )
                    .addClass(i.slideDuplicateActiveClass));
          var l = e
            .nextAll("." + i.slideClass)
            .eq(0)
            .addClass(i.slideNextClass);
          i.loop && 0 === l.length && (l = n.eq(0)).addClass(i.slideNextClass);
          var c = e
            .prevAll("." + i.slideClass)
            .eq(0)
            .addClass(i.slidePrevClass);
          i.loop && 0 === c.length && (c = n.eq(-1)).addClass(i.slidePrevClass),
            i.loop &&
              (l.hasClass(i.slideDuplicateClass)
                ? r
                    .children(
                      "." +
                        i.slideClass +
                        ":not(." +
                        i.slideDuplicateClass +
                        ')[data-swiper-slide-index="' +
                        l.attr("data-swiper-slide-index") +
                        '"]'
                    )
                    .addClass(i.slideDuplicateNextClass)
                : r
                    .children(
                      "." +
                        i.slideClass +
                        "." +
                        i.slideDuplicateClass +
                        '[data-swiper-slide-index="' +
                        l.attr("data-swiper-slide-index") +
                        '"]'
                    )
                    .addClass(i.slideDuplicateNextClass),
              c.hasClass(i.slideDuplicateClass)
                ? r
                    .children(
                      "." +
                        i.slideClass +
                        ":not(." +
                        i.slideDuplicateClass +
                        ')[data-swiper-slide-index="' +
                        c.attr("data-swiper-slide-index") +
                        '"]'
                    )
                    .addClass(i.slideDuplicatePrevClass)
                : r
                    .children(
                      "." +
                        i.slideClass +
                        "." +
                        i.slideDuplicateClass +
                        '[data-swiper-slide-index="' +
                        c.attr("data-swiper-slide-index") +
                        '"]'
                    )
                    .addClass(i.slideDuplicatePrevClass)),
            t.emitSlidesClasses();
        },
        updateActiveIndex: function updateActiveIndex(e) {
          var t,
            n = this,
            i = n.rtlTranslate ? n.translate : -n.translate,
            r = n.slidesGrid,
            a = n.snapGrid,
            o = n.params,
            s = n.activeIndex,
            l = n.realIndex,
            c = n.snapIndex,
            d = e;
          if (void 0 === d) {
            for (var u = 0; u < r.length; u += 1)
              void 0 !== r[u + 1]
                ? i >= r[u] && i < r[u + 1] - (r[u + 1] - r[u]) / 2
                  ? (d = u)
                  : i >= r[u] && i < r[u + 1] && (d = u + 1)
                : i >= r[u] && (d = u);
            o.normalizeSlideIndex && (d < 0 || void 0 === d) && (d = 0);
          }
          if (a.indexOf(i) >= 0) t = a.indexOf(i);
          else {
            var p = Math.min(o.slidesPerGroupSkip, d);
            t = p + Math.floor((d - p) / o.slidesPerGroup);
          }
          if ((t >= a.length && (t = a.length - 1), d !== s)) {
            var h = parseInt(
              n.slides.eq(d).attr("data-swiper-slide-index") || d,
              10
            );
            extend(n, {
              snapIndex: t,
              realIndex: h,
              previousIndex: s,
              activeIndex: d,
            }),
              n.emit("activeIndexChange"),
              n.emit("snapIndexChange"),
              l !== h && n.emit("realIndexChange"),
              (n.initialized || n.params.runCallbacksOnInit) &&
                n.emit("slideChange");
          } else t !== c && ((n.snapIndex = t), n.emit("snapIndexChange"));
        },
        updateClickedSlide: function updateClickedSlide(e) {
          var t,
            n = this,
            i = n.params,
            r = $(e.target).closest("." + i.slideClass)[0],
            a = !1;
          if (r)
            for (var o = 0; o < n.slides.length; o += 1)
              if (n.slides[o] === r) {
                (a = !0), (t = o);
                break;
              }
          if (!r || !a)
            return (n.clickedSlide = void 0), void (n.clickedIndex = void 0);
          (n.clickedSlide = r),
            n.virtual && n.params.virtual.enabled
              ? (n.clickedIndex = parseInt(
                  $(r).attr("data-swiper-slide-index"),
                  10
                ))
              : (n.clickedIndex = t),
            i.slideToClickedSlide &&
              void 0 !== n.clickedIndex &&
              n.clickedIndex !== n.activeIndex &&
              n.slideToClickedSlide();
        },
      };
      var E = {
        getTranslate: function getSwiperTranslate(e) {
          void 0 === e && (e = this.isHorizontal() ? "x" : "y");
          var t = this,
            n = t.params,
            i = t.rtlTranslate,
            r = t.translate,
            a = t.$wrapperEl;
          if (n.virtualTranslate) return i ? -r : r;
          if (n.cssMode) return r;
          var o = getTranslate(a[0], e);
          return i && (o = -o), o || 0;
        },
        setTranslate: function setTranslate(e, t) {
          var n = this,
            i = n.rtlTranslate,
            r = n.params,
            a = n.$wrapperEl,
            o = n.wrapperEl,
            s = n.progress,
            l = 0,
            c = 0;
          n.isHorizontal() ? (l = i ? -e : e) : (c = e),
            r.roundLengths && ((l = Math.floor(l)), (c = Math.floor(c))),
            r.cssMode
              ? (o[n.isHorizontal() ? "scrollLeft" : "scrollTop"] =
                  n.isHorizontal() ? -l : -c)
              : r.virtualTranslate ||
                a.transform("translate3d(" + l + "px, " + c + "px, 0px)"),
            (n.previousTranslate = n.translate),
            (n.translate = n.isHorizontal() ? l : c);
          var d = n.maxTranslate() - n.minTranslate();
          (0 === d ? 0 : (e - n.minTranslate()) / d) !== s &&
            n.updateProgress(e),
            n.emit("setTranslate", n.translate, t);
        },
        minTranslate: function minTranslate() {
          return -this.snapGrid[0];
        },
        maxTranslate: function maxTranslate() {
          return -this.snapGrid[this.snapGrid.length - 1];
        },
        translateTo: function translateTo(e, t, n, i, r) {
          void 0 === e && (e = 0),
            void 0 === t && (t = this.params.speed),
            void 0 === n && (n = !0),
            void 0 === i && (i = !0);
          var a = this,
            o = a.params,
            s = a.wrapperEl;
          if (a.animating && o.preventInteractionOnTransition) return !1;
          var l,
            c = a.minTranslate(),
            d = a.maxTranslate();
          if (
            ((l = i && e > c ? c : i && e < d ? d : e),
            a.updateProgress(l),
            o.cssMode)
          ) {
            var u,
              p = a.isHorizontal();
            if (0 === t) s[p ? "scrollLeft" : "scrollTop"] = -l;
            else if (s.scrollTo)
              s.scrollTo(
                (((u = {})[p ? "left" : "top"] = -l),
                (u.behavior = "smooth"),
                u)
              );
            else s[p ? "scrollLeft" : "scrollTop"] = -l;
            return !0;
          }
          return (
            0 === t
              ? (a.setTransition(0),
                a.setTranslate(l),
                n &&
                  (a.emit("beforeTransitionStart", t, r),
                  a.emit("transitionEnd")))
              : (a.setTransition(t),
                a.setTranslate(l),
                n &&
                  (a.emit("beforeTransitionStart", t, r),
                  a.emit("transitionStart")),
                a.animating ||
                  ((a.animating = !0),
                  a.onTranslateToWrapperTransitionEnd ||
                    (a.onTranslateToWrapperTransitionEnd =
                      function transitionEnd(e) {
                        a &&
                          !a.destroyed &&
                          e.target === this &&
                          (a.$wrapperEl[0].removeEventListener(
                            "transitionend",
                            a.onTranslateToWrapperTransitionEnd
                          ),
                          a.$wrapperEl[0].removeEventListener(
                            "webkitTransitionEnd",
                            a.onTranslateToWrapperTransitionEnd
                          ),
                          (a.onTranslateToWrapperTransitionEnd = null),
                          delete a.onTranslateToWrapperTransitionEnd,
                          n && a.emit("transitionEnd"));
                      }),
                  a.$wrapperEl[0].addEventListener(
                    "transitionend",
                    a.onTranslateToWrapperTransitionEnd
                  ),
                  a.$wrapperEl[0].addEventListener(
                    "webkitTransitionEnd",
                    a.onTranslateToWrapperTransitionEnd
                  ))),
            !0
          );
        },
      };
      var S = {
        slideTo: function slideTo(e, t, n, i, r) {
          if (
            (void 0 === e && (e = 0),
            void 0 === t && (t = this.params.speed),
            void 0 === n && (n = !0),
            "number" != typeof e && "string" != typeof e)
          )
            throw new Error(
              "The 'index' argument cannot have type other than 'number' or 'string'. [" +
                (0, l.A)(e) +
                "] given."
            );
          if ("string" == typeof e) {
            var a = parseInt(e, 10);
            if (!isFinite(a))
              throw new Error(
                "The passed-in 'index' (string) couldn't be converted to 'number'. [" +
                  e +
                  "] given."
              );
            e = a;
          }
          var o = this,
            s = e;
          s < 0 && (s = 0);
          var c = o.params,
            d = o.snapGrid,
            u = o.slidesGrid,
            p = o.previousIndex,
            h = o.activeIndex,
            m = o.rtlTranslate,
            f = o.wrapperEl,
            v = o.enabled;
          if (
            (o.animating && c.preventInteractionOnTransition) ||
            (!v && !i && !r)
          )
            return !1;
          var g = Math.min(o.params.slidesPerGroupSkip, s),
            w = g + Math.floor((s - g) / o.params.slidesPerGroup);
          w >= d.length && (w = d.length - 1),
            (h || c.initialSlide || 0) === (p || 0) &&
              n &&
              o.emit("beforeSlideChangeStart");
          var b,
            y = -d[w];
          if ((o.updateProgress(y), c.normalizeSlideIndex))
            for (var A = 0; A < u.length; A += 1) {
              var x = -Math.floor(100 * y),
                _ = Math.floor(100 * u[A]),
                E = Math.floor(100 * u[A + 1]);
              void 0 !== u[A + 1]
                ? x >= _ && x < E - (E - _) / 2
                  ? (s = A)
                  : x >= _ && x < E && (s = A + 1)
                : x >= _ && (s = A);
            }
          if (o.initialized && s !== h) {
            if (!o.allowSlideNext && y < o.translate && y < o.minTranslate())
              return !1;
            if (
              !o.allowSlidePrev &&
              y > o.translate &&
              y > o.maxTranslate() &&
              (h || 0) !== s
            )
              return !1;
          }
          if (
            ((b = s > h ? "next" : s < h ? "prev" : "reset"),
            (m && -y === o.translate) || (!m && y === o.translate))
          )
            return (
              o.updateActiveIndex(s),
              c.autoHeight && o.updateAutoHeight(),
              o.updateSlidesClasses(),
              "slide" !== c.effect && o.setTranslate(y),
              "reset" !== b && (o.transitionStart(n, b), o.transitionEnd(n, b)),
              !1
            );
          if (c.cssMode) {
            var S,
              C = o.isHorizontal(),
              T = -y;
            if ((m && (T = f.scrollWidth - f.offsetWidth - T), 0 === t))
              f[C ? "scrollLeft" : "scrollTop"] = T;
            else if (f.scrollTo)
              f.scrollTo(
                (((S = {})[C ? "left" : "top"] = T), (S.behavior = "smooth"), S)
              );
            else f[C ? "scrollLeft" : "scrollTop"] = T;
            return !0;
          }
          return (
            0 === t
              ? (o.setTransition(0),
                o.setTranslate(y),
                o.updateActiveIndex(s),
                o.updateSlidesClasses(),
                o.emit("beforeTransitionStart", t, i),
                o.transitionStart(n, b),
                o.transitionEnd(n, b))
              : (o.setTransition(t),
                o.setTranslate(y),
                o.updateActiveIndex(s),
                o.updateSlidesClasses(),
                o.emit("beforeTransitionStart", t, i),
                o.transitionStart(n, b),
                o.animating ||
                  ((o.animating = !0),
                  o.onSlideToWrapperTransitionEnd ||
                    (o.onSlideToWrapperTransitionEnd = function transitionEnd(
                      e
                    ) {
                      o &&
                        !o.destroyed &&
                        e.target === this &&
                        (o.$wrapperEl[0].removeEventListener(
                          "transitionend",
                          o.onSlideToWrapperTransitionEnd
                        ),
                        o.$wrapperEl[0].removeEventListener(
                          "webkitTransitionEnd",
                          o.onSlideToWrapperTransitionEnd
                        ),
                        (o.onSlideToWrapperTransitionEnd = null),
                        delete o.onSlideToWrapperTransitionEnd,
                        o.transitionEnd(n, b));
                    }),
                  o.$wrapperEl[0].addEventListener(
                    "transitionend",
                    o.onSlideToWrapperTransitionEnd
                  ),
                  o.$wrapperEl[0].addEventListener(
                    "webkitTransitionEnd",
                    o.onSlideToWrapperTransitionEnd
                  ))),
            !0
          );
        },
        slideToLoop: function slideToLoop(e, t, n, i) {
          void 0 === e && (e = 0),
            void 0 === t && (t = this.params.speed),
            void 0 === n && (n = !0);
          var r = this,
            a = e;
          return r.params.loop && (a += r.loopedSlides), r.slideTo(a, t, n, i);
        },
        slideNext: function slideNext(e, t, n) {
          void 0 === e && (e = this.params.speed), void 0 === t && (t = !0);
          var i = this,
            r = i.params,
            a = i.animating;
          if (!i.enabled) return i;
          var o = i.activeIndex < r.slidesPerGroupSkip ? 1 : r.slidesPerGroup;
          if (r.loop) {
            if (a && r.loopPreventsSlide) return !1;
            i.loopFix(), (i._clientLeft = i.$wrapperEl[0].clientLeft);
          }
          return i.slideTo(i.activeIndex + o, e, t, n);
        },
        slidePrev: function slidePrev(e, t, n) {
          void 0 === e && (e = this.params.speed), void 0 === t && (t = !0);
          var i = this,
            r = i.params,
            a = i.animating,
            o = i.snapGrid,
            s = i.slidesGrid,
            l = i.rtlTranslate;
          if (!i.enabled) return i;
          if (r.loop) {
            if (a && r.loopPreventsSlide) return !1;
            i.loopFix(), (i._clientLeft = i.$wrapperEl[0].clientLeft);
          }
          function normalize(e) {
            return e < 0 ? -Math.floor(Math.abs(e)) : Math.floor(e);
          }
          var c,
            d = normalize(l ? i.translate : -i.translate),
            u = o.map(function (e) {
              return normalize(e);
            }),
            p = o[u.indexOf(d) - 1];
          return (
            void 0 === p &&
              r.cssMode &&
              o.forEach(function (e) {
                !p && d >= e && (p = e);
              }),
            void 0 !== p && (c = s.indexOf(p)) < 0 && (c = i.activeIndex - 1),
            i.slideTo(c, e, t, n)
          );
        },
        slideReset: function slideReset(e, t, n) {
          return (
            void 0 === e && (e = this.params.speed),
            void 0 === t && (t = !0),
            this.slideTo(this.activeIndex, e, t, n)
          );
        },
        slideToClosest: function slideToClosest(e, t, n, i) {
          void 0 === e && (e = this.params.speed),
            void 0 === t && (t = !0),
            void 0 === i && (i = 0.5);
          var r = this,
            a = r.activeIndex,
            o = Math.min(r.params.slidesPerGroupSkip, a),
            s = o + Math.floor((a - o) / r.params.slidesPerGroup),
            l = r.rtlTranslate ? r.translate : -r.translate;
          if (l >= r.snapGrid[s]) {
            var c = r.snapGrid[s];
            l - c > (r.snapGrid[s + 1] - c) * i &&
              (a += r.params.slidesPerGroup);
          } else {
            var d = r.snapGrid[s - 1];
            l - d <= (r.snapGrid[s] - d) * i && (a -= r.params.slidesPerGroup);
          }
          return (
            (a = Math.max(a, 0)),
            (a = Math.min(a, r.slidesGrid.length - 1)),
            r.slideTo(a, e, t, n)
          );
        },
        slideToClickedSlide: function slideToClickedSlide() {
          var e,
            t = this,
            n = t.params,
            i = t.$wrapperEl,
            r =
              "auto" === n.slidesPerView
                ? t.slidesPerViewDynamic()
                : n.slidesPerView,
            a = t.clickedIndex;
          if (n.loop) {
            if (t.animating) return;
            (e = parseInt(
              $(t.clickedSlide).attr("data-swiper-slide-index"),
              10
            )),
              n.centeredSlides
                ? a < t.loopedSlides - r / 2 ||
                  a > t.slides.length - t.loopedSlides + r / 2
                  ? (t.loopFix(),
                    (a = i
                      .children(
                        "." +
                          n.slideClass +
                          '[data-swiper-slide-index="' +
                          e +
                          '"]:not(.' +
                          n.slideDuplicateClass +
                          ")"
                      )
                      .eq(0)
                      .index()),
                    nextTick(function () {
                      t.slideTo(a);
                    }))
                  : t.slideTo(a)
                : a > t.slides.length - r
                ? (t.loopFix(),
                  (a = i
                    .children(
                      "." +
                        n.slideClass +
                        '[data-swiper-slide-index="' +
                        e +
                        '"]:not(.' +
                        n.slideDuplicateClass +
                        ")"
                    )
                    .eq(0)
                    .index()),
                  nextTick(function () {
                    t.slideTo(a);
                  }))
                : t.slideTo(a);
          } else t.slideTo(a);
        },
      };
      var C = {
        loopCreate: function loopCreate() {
          var e = this,
            t = getDocument(),
            n = e.params,
            i = e.$wrapperEl;
          i.children("." + n.slideClass + "." + n.slideDuplicateClass).remove();
          var r = i.children("." + n.slideClass);
          if (n.loopFillGroupWithBlank) {
            var a = n.slidesPerGroup - (r.length % n.slidesPerGroup);
            if (a !== n.slidesPerGroup) {
              for (var o = 0; o < a; o += 1) {
                var s = $(t.createElement("div")).addClass(
                  n.slideClass + " " + n.slideBlankClass
                );
                i.append(s);
              }
              r = i.children("." + n.slideClass);
            }
          }
          "auto" !== n.slidesPerView ||
            n.loopedSlides ||
            (n.loopedSlides = r.length),
            (e.loopedSlides = Math.ceil(
              parseFloat(n.loopedSlides || n.slidesPerView, 10)
            )),
            (e.loopedSlides += n.loopAdditionalSlides),
            e.loopedSlides > r.length && (e.loopedSlides = r.length);
          var l = [],
            c = [];
          r.each(function (t, n) {
            var i = $(t);
            n < e.loopedSlides && c.push(t),
              n < r.length && n >= r.length - e.loopedSlides && l.push(t),
              i.attr("data-swiper-slide-index", n);
          });
          for (var d = 0; d < c.length; d += 1)
            i.append($(c[d].cloneNode(!0)).addClass(n.slideDuplicateClass));
          for (var u = l.length - 1; u >= 0; u -= 1)
            i.prepend($(l[u].cloneNode(!0)).addClass(n.slideDuplicateClass));
        },
        loopFix: function loopFix() {
          var e = this;
          e.emit("beforeLoopFix");
          var t,
            n = e.activeIndex,
            i = e.slides,
            r = e.loopedSlides,
            a = e.allowSlidePrev,
            o = e.allowSlideNext,
            s = e.snapGrid,
            l = e.rtlTranslate;
          (e.allowSlidePrev = !0), (e.allowSlideNext = !0);
          var c = -s[n] - e.getTranslate();
          if (n < r)
            (t = i.length - 3 * r + n),
              (t += r),
              e.slideTo(t, 0, !1, !0) &&
                0 !== c &&
                e.setTranslate((l ? -e.translate : e.translate) - c);
          else if (n >= i.length - r) {
            (t = -i.length + n + r),
              (t += r),
              e.slideTo(t, 0, !1, !0) &&
                0 !== c &&
                e.setTranslate((l ? -e.translate : e.translate) - c);
          }
          (e.allowSlidePrev = a), (e.allowSlideNext = o), e.emit("loopFix");
        },
        loopDestroy: function loopDestroy() {
          var e = this,
            t = e.$wrapperEl,
            n = e.params,
            i = e.slides;
          t
            .children(
              "." +
                n.slideClass +
                "." +
                n.slideDuplicateClass +
                ",." +
                n.slideClass +
                "." +
                n.slideBlankClass
            )
            .remove(),
            i.removeAttr("data-swiper-slide-index");
        },
      };
      var T = {
        appendSlide: function appendSlide(e) {
          var t = this,
            n = t.$wrapperEl,
            i = t.params;
          if (
            (i.loop && t.loopDestroy(),
            "object" === (0, l.A)(e) && "length" in e)
          )
            for (var r = 0; r < e.length; r += 1) e[r] && n.append(e[r]);
          else n.append(e);
          i.loop && t.loopCreate(),
            (i.observer && t.support.observer) || t.update();
        },
        prependSlide: function prependSlide(e) {
          var t = this,
            n = t.params,
            i = t.$wrapperEl,
            r = t.activeIndex;
          n.loop && t.loopDestroy();
          var a = r + 1;
          if ("object" === (0, l.A)(e) && "length" in e) {
            for (var o = 0; o < e.length; o += 1) e[o] && i.prepend(e[o]);
            a = r + e.length;
          } else i.prepend(e);
          n.loop && t.loopCreate(),
            (n.observer && t.support.observer) || t.update(),
            t.slideTo(a, 0, !1);
        },
        addSlide: function addSlide(e, t) {
          var n = this,
            i = n.$wrapperEl,
            r = n.params,
            a = n.activeIndex;
          r.loop &&
            ((a -= n.loopedSlides),
            n.loopDestroy(),
            (n.slides = i.children("." + r.slideClass)));
          var o = n.slides.length;
          if (e <= 0) n.prependSlide(t);
          else if (e >= o) n.appendSlide(t);
          else {
            for (var s = a > e ? a + 1 : a, c = [], d = o - 1; d >= e; d -= 1) {
              var u = n.slides.eq(d);
              u.remove(), c.unshift(u);
            }
            if ("object" === (0, l.A)(t) && "length" in t) {
              for (var p = 0; p < t.length; p += 1) t[p] && i.append(t[p]);
              s = a > e ? a + t.length : a;
            } else i.append(t);
            for (var h = 0; h < c.length; h += 1) i.append(c[h]);
            r.loop && n.loopCreate(),
              (r.observer && n.support.observer) || n.update(),
              r.loop
                ? n.slideTo(s + n.loopedSlides, 0, !1)
                : n.slideTo(s, 0, !1);
          }
        },
        removeSlide: function removeSlide(e) {
          var t = this,
            n = t.params,
            i = t.$wrapperEl,
            r = t.activeIndex;
          n.loop &&
            ((r -= t.loopedSlides),
            t.loopDestroy(),
            (t.slides = i.children("." + n.slideClass)));
          var a,
            o = r;
          if ("object" === (0, l.A)(e) && "length" in e) {
            for (var s = 0; s < e.length; s += 1)
              (a = e[s]),
                t.slides[a] && t.slides.eq(a).remove(),
                a < o && (o -= 1);
            o = Math.max(o, 0);
          } else
            (a = e),
              t.slides[a] && t.slides.eq(a).remove(),
              a < o && (o -= 1),
              (o = Math.max(o, 0));
          n.loop && t.loopCreate(),
            (n.observer && t.support.observer) || t.update(),
            n.loop ? t.slideTo(o + t.loopedSlides, 0, !1) : t.slideTo(o, 0, !1);
        },
        removeAllSlides: function removeAllSlides() {
          for (var e = [], t = 0; t < this.slides.length; t += 1) e.push(t);
          this.removeSlide(e);
        },
      };
      function onTouchStart(e) {
        var t = this,
          n = getDocument(),
          i = getWindow(),
          r = t.touchEventsData,
          a = t.params,
          o = t.touches;
        if (t.enabled && (!t.animating || !a.preventInteractionOnTransition)) {
          var s = e;
          s.originalEvent && (s = s.originalEvent);
          var l = $(s.target);
          if (
            "wrapper" !== a.touchEventsTarget ||
            l.closest(t.wrapperEl).length
          )
            if (
              ((r.isTouchEvent = "touchstart" === s.type),
              r.isTouchEvent || !("which" in s) || 3 !== s.which)
            )
              if (!(!r.isTouchEvent && "button" in s && s.button > 0))
                if (!r.isTouched || !r.isMoved)
                  if (
                    (!!a.noSwipingClass &&
                      "" !== a.noSwipingClass &&
                      s.target &&
                      s.target.shadowRoot &&
                      e.path &&
                      e.path[0] &&
                      (l = $(e.path[0])),
                    a.noSwiping &&
                      l.closest(
                        a.noSwipingSelector
                          ? a.noSwipingSelector
                          : "." + a.noSwipingClass
                      )[0])
                  )
                    t.allowClick = !0;
                  else if (!a.swipeHandler || l.closest(a.swipeHandler)[0]) {
                    (o.currentX =
                      "touchstart" === s.type
                        ? s.targetTouches[0].pageX
                        : s.pageX),
                      (o.currentY =
                        "touchstart" === s.type
                          ? s.targetTouches[0].pageY
                          : s.pageY);
                    var c = o.currentX,
                      d = o.currentY,
                      u = a.edgeSwipeDetection || a.iOSEdgeSwipeDetection,
                      p = a.edgeSwipeThreshold || a.iOSEdgeSwipeThreshold;
                    if (u && (c <= p || c >= i.innerWidth - p)) {
                      if ("prevent" !== u) return;
                      e.preventDefault();
                    }
                    if (
                      (extend(r, {
                        isTouched: !0,
                        isMoved: !1,
                        allowTouchCallbacks: !0,
                        isScrolling: void 0,
                        startMoving: void 0,
                      }),
                      (o.startX = c),
                      (o.startY = d),
                      (r.touchStartTime = now()),
                      (t.allowClick = !0),
                      t.updateSize(),
                      (t.swipeDirection = void 0),
                      a.threshold > 0 && (r.allowThresholdMove = !1),
                      "touchstart" !== s.type)
                    ) {
                      var h = !0;
                      l.is(r.focusableElements) && (h = !1),
                        n.activeElement &&
                          $(n.activeElement).is(r.focusableElements) &&
                          n.activeElement !== l[0] &&
                          n.activeElement.blur();
                      var m =
                        h && t.allowTouchMove && a.touchStartPreventDefault;
                      (!a.touchStartForcePreventDefault && !m) ||
                        l[0].isContentEditable ||
                        s.preventDefault();
                    }
                    t.emit("touchStart", s);
                  }
        }
      }
      function onTouchMove(e) {
        var t = getDocument(),
          n = this,
          i = n.touchEventsData,
          r = n.params,
          a = n.touches,
          o = n.rtlTranslate;
        if (n.enabled) {
          var s = e;
          if ((s.originalEvent && (s = s.originalEvent), i.isTouched)) {
            if (!i.isTouchEvent || "touchmove" === s.type) {
              var l =
                  "touchmove" === s.type &&
                  s.targetTouches &&
                  (s.targetTouches[0] || s.changedTouches[0]),
                c = "touchmove" === s.type ? l.pageX : s.pageX,
                d = "touchmove" === s.type ? l.pageY : s.pageY;
              if (s.preventedByNestedSwiper)
                return (a.startX = c), void (a.startY = d);
              if (!n.allowTouchMove)
                return (
                  (n.allowClick = !1),
                  void (
                    i.isTouched &&
                    (extend(a, {
                      startX: c,
                      startY: d,
                      currentX: c,
                      currentY: d,
                    }),
                    (i.touchStartTime = now()))
                  )
                );
              if (i.isTouchEvent && r.touchReleaseOnEdges && !r.loop)
                if (n.isVertical()) {
                  if (
                    (d < a.startY && n.translate <= n.maxTranslate()) ||
                    (d > a.startY && n.translate >= n.minTranslate())
                  )
                    return (i.isTouched = !1), void (i.isMoved = !1);
                } else if (
                  (c < a.startX && n.translate <= n.maxTranslate()) ||
                  (c > a.startX && n.translate >= n.minTranslate())
                )
                  return;
              if (
                i.isTouchEvent &&
                t.activeElement &&
                s.target === t.activeElement &&
                $(s.target).is(i.focusableElements)
              )
                return (i.isMoved = !0), void (n.allowClick = !1);
              if (
                (i.allowTouchCallbacks && n.emit("touchMove", s),
                !(s.targetTouches && s.targetTouches.length > 1))
              ) {
                (a.currentX = c), (a.currentY = d);
                var u = a.currentX - a.startX,
                  p = a.currentY - a.startY;
                if (
                  !(
                    n.params.threshold &&
                    Math.sqrt(Math.pow(u, 2) + Math.pow(p, 2)) <
                      n.params.threshold
                  )
                ) {
                  var h;
                  if (void 0 === i.isScrolling)
                    (n.isHorizontal() && a.currentY === a.startY) ||
                    (n.isVertical() && a.currentX === a.startX)
                      ? (i.isScrolling = !1)
                      : u * u + p * p >= 25 &&
                        ((h =
                          (180 * Math.atan2(Math.abs(p), Math.abs(u))) /
                          Math.PI),
                        (i.isScrolling = n.isHorizontal()
                          ? h > r.touchAngle
                          : 90 - h > r.touchAngle));
                  if (
                    (i.isScrolling && n.emit("touchMoveOpposite", s),
                    void 0 === i.startMoving &&
                      ((a.currentX === a.startX && a.currentY === a.startY) ||
                        (i.startMoving = !0)),
                    i.isScrolling)
                  )
                    i.isTouched = !1;
                  else if (i.startMoving) {
                    (n.allowClick = !1),
                      !r.cssMode && s.cancelable && s.preventDefault(),
                      r.touchMoveStopPropagation &&
                        !r.nested &&
                        s.stopPropagation(),
                      i.isMoved ||
                        (r.loop && n.loopFix(),
                        (i.startTranslate = n.getTranslate()),
                        n.setTransition(0),
                        n.animating &&
                          n.$wrapperEl.trigger(
                            "webkitTransitionEnd transitionend"
                          ),
                        (i.allowMomentumBounce = !1),
                        !r.grabCursor ||
                          (!0 !== n.allowSlideNext &&
                            !0 !== n.allowSlidePrev) ||
                          n.setGrabCursor(!0),
                        n.emit("sliderFirstMove", s)),
                      n.emit("sliderMove", s),
                      (i.isMoved = !0);
                    var m = n.isHorizontal() ? u : p;
                    (a.diff = m),
                      (m *= r.touchRatio),
                      o && (m = -m),
                      (n.swipeDirection = m > 0 ? "prev" : "next"),
                      (i.currentTranslate = m + i.startTranslate);
                    var f = !0,
                      v = r.resistanceRatio;
                    if (
                      (r.touchReleaseOnEdges && (v = 0),
                      m > 0 && i.currentTranslate > n.minTranslate()
                        ? ((f = !1),
                          r.resistance &&
                            (i.currentTranslate =
                              n.minTranslate() -
                              1 +
                              Math.pow(
                                -n.minTranslate() + i.startTranslate + m,
                                v
                              )))
                        : m < 0 &&
                          i.currentTranslate < n.maxTranslate() &&
                          ((f = !1),
                          r.resistance &&
                            (i.currentTranslate =
                              n.maxTranslate() +
                              1 -
                              Math.pow(
                                n.maxTranslate() - i.startTranslate - m,
                                v
                              ))),
                      f && (s.preventedByNestedSwiper = !0),
                      !n.allowSlideNext &&
                        "next" === n.swipeDirection &&
                        i.currentTranslate < i.startTranslate &&
                        (i.currentTranslate = i.startTranslate),
                      !n.allowSlidePrev &&
                        "prev" === n.swipeDirection &&
                        i.currentTranslate > i.startTranslate &&
                        (i.currentTranslate = i.startTranslate),
                      n.allowSlidePrev ||
                        n.allowSlideNext ||
                        (i.currentTranslate = i.startTranslate),
                      r.threshold > 0)
                    ) {
                      if (!(Math.abs(m) > r.threshold || i.allowThresholdMove))
                        return void (i.currentTranslate = i.startTranslate);
                      if (!i.allowThresholdMove)
                        return (
                          (i.allowThresholdMove = !0),
                          (a.startX = a.currentX),
                          (a.startY = a.currentY),
                          (i.currentTranslate = i.startTranslate),
                          void (a.diff = n.isHorizontal()
                            ? a.currentX - a.startX
                            : a.currentY - a.startY)
                        );
                    }
                    r.followFinger &&
                      !r.cssMode &&
                      ((r.freeMode ||
                        r.watchSlidesProgress ||
                        r.watchSlidesVisibility) &&
                        (n.updateActiveIndex(), n.updateSlidesClasses()),
                      r.freeMode &&
                        (0 === i.velocities.length &&
                          i.velocities.push({
                            position: a[n.isHorizontal() ? "startX" : "startY"],
                            time: i.touchStartTime,
                          }),
                        i.velocities.push({
                          position:
                            a[n.isHorizontal() ? "currentX" : "currentY"],
                          time: now(),
                        })),
                      n.updateProgress(i.currentTranslate),
                      n.setTranslate(i.currentTranslate));
                  }
                }
              }
            }
          } else
            i.startMoving && i.isScrolling && n.emit("touchMoveOpposite", s);
        }
      }
      function onTouchEnd(e) {
        var t = this,
          n = t.touchEventsData,
          i = t.params,
          r = t.touches,
          a = t.rtlTranslate,
          o = t.$wrapperEl,
          s = t.slidesGrid,
          l = t.snapGrid;
        if (t.enabled) {
          var c = e;
          if (
            (c.originalEvent && (c = c.originalEvent),
            n.allowTouchCallbacks && t.emit("touchEnd", c),
            (n.allowTouchCallbacks = !1),
            !n.isTouched)
          )
            return (
              n.isMoved && i.grabCursor && t.setGrabCursor(!1),
              (n.isMoved = !1),
              void (n.startMoving = !1)
            );
          i.grabCursor &&
            n.isMoved &&
            n.isTouched &&
            (!0 === t.allowSlideNext || !0 === t.allowSlidePrev) &&
            t.setGrabCursor(!1);
          var d,
            u = now(),
            p = u - n.touchStartTime;
          if (
            (t.allowClick &&
              (t.updateClickedSlide(c),
              t.emit("tap click", c),
              p < 300 &&
                u - n.lastClickTime < 300 &&
                t.emit("doubleTap doubleClick", c)),
            (n.lastClickTime = now()),
            nextTick(function () {
              t.destroyed || (t.allowClick = !0);
            }),
            !n.isTouched ||
              !n.isMoved ||
              !t.swipeDirection ||
              0 === r.diff ||
              n.currentTranslate === n.startTranslate)
          )
            return (
              (n.isTouched = !1), (n.isMoved = !1), void (n.startMoving = !1)
            );
          if (
            ((n.isTouched = !1),
            (n.isMoved = !1),
            (n.startMoving = !1),
            (d = i.followFinger
              ? a
                ? t.translate
                : -t.translate
              : -n.currentTranslate),
            !i.cssMode)
          )
            if (i.freeMode) {
              if (d < -t.minTranslate()) return void t.slideTo(t.activeIndex);
              if (d > -t.maxTranslate())
                return void (t.slides.length < l.length
                  ? t.slideTo(l.length - 1)
                  : t.slideTo(t.slides.length - 1));
              if (i.freeModeMomentum) {
                if (n.velocities.length > 1) {
                  var h = n.velocities.pop(),
                    m = n.velocities.pop(),
                    f = h.position - m.position,
                    v = h.time - m.time;
                  (t.velocity = f / v),
                    (t.velocity /= 2),
                    Math.abs(t.velocity) < i.freeModeMinimumVelocity &&
                      (t.velocity = 0),
                    (v > 150 || now() - h.time > 300) && (t.velocity = 0);
                } else t.velocity = 0;
                (t.velocity *= i.freeModeMomentumVelocityRatio),
                  (n.velocities.length = 0);
                var g = 1e3 * i.freeModeMomentumRatio,
                  w = t.velocity * g,
                  b = t.translate + w;
                a && (b = -b);
                var y,
                  A,
                  x = !1,
                  _ = 20 * Math.abs(t.velocity) * i.freeModeMomentumBounceRatio;
                if (b < t.maxTranslate())
                  i.freeModeMomentumBounce
                    ? (b + t.maxTranslate() < -_ && (b = t.maxTranslate() - _),
                      (y = t.maxTranslate()),
                      (x = !0),
                      (n.allowMomentumBounce = !0))
                    : (b = t.maxTranslate()),
                    i.loop && i.centeredSlides && (A = !0);
                else if (b > t.minTranslate())
                  i.freeModeMomentumBounce
                    ? (b - t.minTranslate() > _ && (b = t.minTranslate() + _),
                      (y = t.minTranslate()),
                      (x = !0),
                      (n.allowMomentumBounce = !0))
                    : (b = t.minTranslate()),
                    i.loop && i.centeredSlides && (A = !0);
                else if (i.freeModeSticky) {
                  for (var E, S = 0; S < l.length; S += 1)
                    if (l[S] > -b) {
                      E = S;
                      break;
                    }
                  b = -(b =
                    Math.abs(l[E] - b) < Math.abs(l[E - 1] - b) ||
                    "next" === t.swipeDirection
                      ? l[E]
                      : l[E - 1]);
                }
                if (
                  (A &&
                    t.once("transitionEnd", function () {
                      t.loopFix();
                    }),
                  0 !== t.velocity)
                ) {
                  if (
                    ((g = a
                      ? Math.abs((-b - t.translate) / t.velocity)
                      : Math.abs((b - t.translate) / t.velocity)),
                    i.freeModeSticky)
                  ) {
                    var C = Math.abs((a ? -b : b) - t.translate),
                      T = t.slidesSizesGrid[t.activeIndex];
                    g =
                      C < T
                        ? i.speed
                        : C < 2 * T
                        ? 1.5 * i.speed
                        : 2.5 * i.speed;
                  }
                } else if (i.freeModeSticky) return void t.slideToClosest();
                i.freeModeMomentumBounce && x
                  ? (t.updateProgress(y),
                    t.setTransition(g),
                    t.setTranslate(b),
                    t.transitionStart(!0, t.swipeDirection),
                    (t.animating = !0),
                    o.transitionEnd(function () {
                      t &&
                        !t.destroyed &&
                        n.allowMomentumBounce &&
                        (t.emit("momentumBounce"),
                        t.setTransition(i.speed),
                        setTimeout(function () {
                          t.setTranslate(y),
                            o.transitionEnd(function () {
                              t && !t.destroyed && t.transitionEnd();
                            });
                        }, 0));
                    }))
                  : t.velocity
                  ? (t.updateProgress(b),
                    t.setTransition(g),
                    t.setTranslate(b),
                    t.transitionStart(!0, t.swipeDirection),
                    t.animating ||
                      ((t.animating = !0),
                      o.transitionEnd(function () {
                        t && !t.destroyed && t.transitionEnd();
                      })))
                  : (t.emit("_freeModeNoMomentumRelease"), t.updateProgress(b)),
                  t.updateActiveIndex(),
                  t.updateSlidesClasses();
              } else {
                if (i.freeModeSticky) return void t.slideToClosest();
                i.freeMode && t.emit("_freeModeNoMomentumRelease");
              }
              (!i.freeModeMomentum || p >= i.longSwipesMs) &&
                (t.updateProgress(),
                t.updateActiveIndex(),
                t.updateSlidesClasses());
            } else {
              for (
                var k = 0, M = t.slidesSizesGrid[0], I = 0;
                I < s.length;
                I += I < i.slidesPerGroupSkip ? 1 : i.slidesPerGroup
              ) {
                var L = I < i.slidesPerGroupSkip - 1 ? 1 : i.slidesPerGroup;
                void 0 !== s[I + L]
                  ? d >= s[I] &&
                    d < s[I + L] &&
                    ((k = I), (M = s[I + L] - s[I]))
                  : d >= s[I] &&
                    ((k = I), (M = s[s.length - 1] - s[s.length - 2]));
              }
              var P = (d - s[k]) / M,
                N = k < i.slidesPerGroupSkip - 1 ? 1 : i.slidesPerGroup;
              if (p > i.longSwipesMs) {
                if (!i.longSwipes) return void t.slideTo(t.activeIndex);
                "next" === t.swipeDirection &&
                  (P >= i.longSwipesRatio ? t.slideTo(k + N) : t.slideTo(k)),
                  "prev" === t.swipeDirection &&
                    (P > 1 - i.longSwipesRatio
                      ? t.slideTo(k + N)
                      : t.slideTo(k));
              } else {
                if (!i.shortSwipes) return void t.slideTo(t.activeIndex);
                t.navigation &&
                (c.target === t.navigation.nextEl ||
                  c.target === t.navigation.prevEl)
                  ? c.target === t.navigation.nextEl
                    ? t.slideTo(k + N)
                    : t.slideTo(k)
                  : ("next" === t.swipeDirection && t.slideTo(k + N),
                    "prev" === t.swipeDirection && t.slideTo(k));
              }
            }
        }
      }
      function onResize() {
        var e = this,
          t = e.params,
          n = e.el;
        if (!n || 0 !== n.offsetWidth) {
          t.breakpoints && e.setBreakpoint();
          var i = e.allowSlideNext,
            r = e.allowSlidePrev,
            a = e.snapGrid;
          (e.allowSlideNext = !0),
            (e.allowSlidePrev = !0),
            e.updateSize(),
            e.updateSlides(),
            e.updateSlidesClasses(),
            ("auto" === t.slidesPerView || t.slidesPerView > 1) &&
            e.isEnd &&
            !e.isBeginning &&
            !e.params.centeredSlides
              ? e.slideTo(e.slides.length - 1, 0, !1, !0)
              : e.slideTo(e.activeIndex, 0, !1, !0),
            e.autoplay &&
              e.autoplay.running &&
              e.autoplay.paused &&
              e.autoplay.run(),
            (e.allowSlidePrev = r),
            (e.allowSlideNext = i),
            e.params.watchOverflow && a !== e.snapGrid && e.checkOverflow();
        }
      }
      function onClick(e) {
        var t = this;
        t.enabled &&
          (t.allowClick ||
            (t.params.preventClicks && e.preventDefault(),
            t.params.preventClicksPropagation &&
              t.animating &&
              (e.stopPropagation(), e.stopImmediatePropagation())));
      }
      function onScroll() {
        var e = this,
          t = e.wrapperEl,
          n = e.rtlTranslate;
        if (e.enabled) {
          (e.previousTranslate = e.translate),
            e.isHorizontal()
              ? (e.translate = n
                  ? t.scrollWidth - t.offsetWidth - t.scrollLeft
                  : -t.scrollLeft)
              : (e.translate = -t.scrollTop),
            -0 === e.translate && (e.translate = 0),
            e.updateActiveIndex(),
            e.updateSlidesClasses();
          var i = e.maxTranslate() - e.minTranslate();
          (0 === i ? 0 : (e.translate - e.minTranslate()) / i) !== e.progress &&
            e.updateProgress(n ? -e.translate : e.translate),
            e.emit("setTranslate", e.translate, !1);
        }
      }
      var k = !1;
      function dummyEventListener() {}
      var M = {
        attachEvents: function attachEvents() {
          var e = this,
            t = getDocument(),
            n = e.params,
            i = e.touchEvents,
            r = e.el,
            a = e.wrapperEl,
            o = e.device,
            s = e.support;
          (e.onTouchStart = onTouchStart.bind(e)),
            (e.onTouchMove = onTouchMove.bind(e)),
            (e.onTouchEnd = onTouchEnd.bind(e)),
            n.cssMode && (e.onScroll = onScroll.bind(e)),
            (e.onClick = onClick.bind(e));
          var l = !!n.nested;
          if (!s.touch && s.pointerEvents)
            r.addEventListener(i.start, e.onTouchStart, !1),
              t.addEventListener(i.move, e.onTouchMove, l),
              t.addEventListener(i.end, e.onTouchEnd, !1);
          else {
            if (s.touch) {
              var c = !(
                "touchstart" !== i.start ||
                !s.passiveListener ||
                !n.passiveListeners
              ) && {
                passive: !0,
                capture: !1,
              };
              r.addEventListener(i.start, e.onTouchStart, c),
                r.addEventListener(
                  i.move,
                  e.onTouchMove,
                  s.passiveListener
                    ? {
                        passive: !1,
                        capture: l,
                      }
                    : l
                ),
                r.addEventListener(i.end, e.onTouchEnd, c),
                i.cancel && r.addEventListener(i.cancel, e.onTouchEnd, c),
                k ||
                  (t.addEventListener("touchstart", dummyEventListener),
                  (k = !0));
            }
            ((n.simulateTouch && !o.ios && !o.android) ||
              (n.simulateTouch && !s.touch && o.ios)) &&
              (r.addEventListener("mousedown", e.onTouchStart, !1),
              t.addEventListener("mousemove", e.onTouchMove, l),
              t.addEventListener("mouseup", e.onTouchEnd, !1));
          }
          (n.preventClicks || n.preventClicksPropagation) &&
            r.addEventListener("click", e.onClick, !0),
            n.cssMode && a.addEventListener("scroll", e.onScroll),
            n.updateOnWindowResize
              ? e.on(
                  o.ios || o.android
                    ? "resize orientationchange observerUpdate"
                    : "resize observerUpdate",
                  onResize,
                  !0
                )
              : e.on("observerUpdate", onResize, !0);
        },
        detachEvents: function detachEvents() {
          var e = this,
            t = getDocument(),
            n = e.params,
            i = e.touchEvents,
            r = e.el,
            a = e.wrapperEl,
            o = e.device,
            s = e.support,
            l = !!n.nested;
          if (!s.touch && s.pointerEvents)
            r.removeEventListener(i.start, e.onTouchStart, !1),
              t.removeEventListener(i.move, e.onTouchMove, l),
              t.removeEventListener(i.end, e.onTouchEnd, !1);
          else {
            if (s.touch) {
              var c = !(
                "onTouchStart" !== i.start ||
                !s.passiveListener ||
                !n.passiveListeners
              ) && {
                passive: !0,
                capture: !1,
              };
              r.removeEventListener(i.start, e.onTouchStart, c),
                r.removeEventListener(i.move, e.onTouchMove, l),
                r.removeEventListener(i.end, e.onTouchEnd, c),
                i.cancel && r.removeEventListener(i.cancel, e.onTouchEnd, c);
            }
            ((n.simulateTouch && !o.ios && !o.android) ||
              (n.simulateTouch && !s.touch && o.ios)) &&
              (r.removeEventListener("mousedown", e.onTouchStart, !1),
              t.removeEventListener("mousemove", e.onTouchMove, l),
              t.removeEventListener("mouseup", e.onTouchEnd, !1));
          }
          (n.preventClicks || n.preventClicksPropagation) &&
            r.removeEventListener("click", e.onClick, !0),
            n.cssMode && a.removeEventListener("scroll", e.onScroll),
            e.off(
              o.ios || o.android
                ? "resize orientationchange observerUpdate"
                : "resize observerUpdate",
              onResize
            );
        },
      };
      var I = {
        addClasses: function addClasses() {
          var e = this,
            t = e.classNames,
            n = e.params,
            i = e.rtl,
            r = e.$el,
            a = e.device,
            o = e.support,
            s = (function prepareClasses(e, t) {
              var n = [];
              return (
                e.forEach(function (e) {
                  "object" === (0, l.A)(e)
                    ? Object.keys(e).forEach(function (i) {
                        e[i] && n.push(t + i);
                      })
                    : "string" == typeof e && n.push(t + e);
                }),
                n
              );
            })(
              [
                "initialized",
                n.direction,
                {
                  "pointer-events": o.pointerEvents && !o.touch,
                },
                {
                  "free-mode": n.freeMode,
                },
                {
                  autoheight: n.autoHeight,
                },
                {
                  rtl: i,
                },
                {
                  multirow: n.slidesPerColumn > 1,
                },
                {
                  "multirow-column":
                    n.slidesPerColumn > 1 && "column" === n.slidesPerColumnFill,
                },
                {
                  android: a.android,
                },
                {
                  ios: a.ios,
                },
                {
                  "css-mode": n.cssMode,
                },
              ],
              n.containerModifierClass
            );
          t.push.apply(t, s),
            r.addClass([].concat(t).join(" ")),
            e.emitContainerClasses();
        },
        removeClasses: function removeClasses() {
          var e = this,
            t = e.$el,
            n = e.classNames;
          t.removeClass(n.join(" ")), e.emitContainerClasses();
        },
      };
      var L = {
        init: !0,
        direction: "horizontal",
        touchEventsTarget: "container",
        initialSlide: 0,
        speed: 300,
        cssMode: !1,
        updateOnWindowResize: !0,
        resizeObserver: !1,
        nested: !1,
        createElements: !1,
        enabled: !0,
        focusableElements:
          "input, select, option, textarea, button, video, label",
        width: null,
        height: null,
        preventInteractionOnTransition: !1,
        userAgent: null,
        url: null,
        edgeSwipeDetection: !1,
        edgeSwipeThreshold: 20,
        freeMode: !1,
        freeModeMomentum: !0,
        freeModeMomentumRatio: 1,
        freeModeMomentumBounce: !0,
        freeModeMomentumBounceRatio: 1,
        freeModeMomentumVelocityRatio: 1,
        freeModeSticky: !1,
        freeModeMinimumVelocity: 0.02,
        autoHeight: !1,
        setWrapperSize: !1,
        virtualTranslate: !1,
        effect: "slide",
        breakpoints: void 0,
        breakpointsBase: "window",
        spaceBetween: 0,
        slidesPerView: 1,
        slidesPerColumn: 1,
        slidesPerColumnFill: "column",
        slidesPerGroup: 1,
        slidesPerGroupSkip: 0,
        centeredSlides: !1,
        centeredSlidesBounds: !1,
        slidesOffsetBefore: 0,
        slidesOffsetAfter: 0,
        normalizeSlideIndex: !0,
        centerInsufficientSlides: !1,
        watchOverflow: !1,
        roundLengths: !1,
        touchRatio: 1,
        touchAngle: 45,
        simulateTouch: !0,
        shortSwipes: !0,
        longSwipes: !0,
        longSwipesRatio: 0.5,
        longSwipesMs: 300,
        followFinger: !0,
        allowTouchMove: !0,
        threshold: 0,
        touchMoveStopPropagation: !1,
        touchStartPreventDefault: !0,
        touchStartForcePreventDefault: !1,
        touchReleaseOnEdges: !1,
        uniqueNavElements: !0,
        resistance: !0,
        resistanceRatio: 0.85,
        watchSlidesProgress: !1,
        watchSlidesVisibility: !1,
        grabCursor: !1,
        preventClicks: !0,
        preventClicksPropagation: !0,
        slideToClickedSlide: !1,
        preloadImages: !0,
        updateOnImagesReady: !0,
        loop: !1,
        loopAdditionalSlides: 0,
        loopedSlides: null,
        loopFillGroupWithBlank: !1,
        loopPreventsSlide: !0,
        allowSlidePrev: !0,
        allowSlideNext: !0,
        swipeHandler: null,
        noSwiping: !0,
        noSwipingClass: "swiper-no-swiping",
        noSwipingSelector: null,
        passiveListeners: !0,
        containerModifierClass: "swiper-container-",
        slideClass: "swiper-slide",
        slideBlankClass: "swiper-slide-invisible-blank",
        slideActiveClass: "swiper-slide-active",
        slideDuplicateActiveClass: "swiper-slide-duplicate-active",
        slideVisibleClass: "swiper-slide-visible",
        slideDuplicateClass: "swiper-slide-duplicate",
        slideNextClass: "swiper-slide-next",
        slideDuplicateNextClass: "swiper-slide-duplicate-next",
        slidePrevClass: "swiper-slide-prev",
        slideDuplicatePrevClass: "swiper-slide-duplicate-prev",
        wrapperClass: "swiper-wrapper",
        runCallbacksOnInit: !0,
        _emitClasses: !1,
      };
      function _defineProperties(e, t) {
        for (var n = 0; n < t.length; n++) {
          var i = t[n];
          (i.enumerable = i.enumerable || !1),
            (i.configurable = !0),
            "value" in i && (i.writable = !0),
            Object.defineProperty(e, i.key, i);
        }
      }
      var P = {
          modular: {
            useParams: function useParams(e) {
              var t = this;
              t.modules &&
                Object.keys(t.modules).forEach(function (n) {
                  var i = t.modules[n];
                  i.params && extend(e, i.params);
                });
            },
            useModules: function useModules(e) {
              void 0 === e && (e = {});
              var t = this;
              t.modules &&
                Object.keys(t.modules).forEach(function (n) {
                  var i = t.modules[n],
                    r = e[n] || {};
                  i.on &&
                    t.on &&
                    Object.keys(i.on).forEach(function (e) {
                      t.on(e, i.on[e]);
                    }),
                    i.create && i.create.bind(t)(r);
                });
            },
          },
          eventsEmitter: x,
          update: _,
          translate: E,
          transition: {
            setTransition: function setTransition(e, t) {
              var n = this;
              n.params.cssMode || n.$wrapperEl.transition(e),
                n.emit("setTransition", e, t);
            },
            transitionStart: function transitionStart(e, t) {
              void 0 === e && (e = !0);
              var n = this,
                i = n.activeIndex,
                r = n.params,
                a = n.previousIndex;
              if (!r.cssMode) {
                r.autoHeight && n.updateAutoHeight();
                var o = t;
                if (
                  (o || (o = i > a ? "next" : i < a ? "prev" : "reset"),
                  n.emit("transitionStart"),
                  e && i !== a)
                ) {
                  if ("reset" === o)
                    return void n.emit("slideResetTransitionStart");
                  n.emit("slideChangeTransitionStart"),
                    "next" === o
                      ? n.emit("slideNextTransitionStart")
                      : n.emit("slidePrevTransitionStart");
                }
              }
            },
            transitionEnd: function transitionEnd(e, t) {
              void 0 === e && (e = !0);
              var n = this,
                i = n.activeIndex,
                r = n.previousIndex,
                a = n.params;
              if (((n.animating = !1), !a.cssMode)) {
                n.setTransition(0);
                var o = t;
                if (
                  (o || (o = i > r ? "next" : i < r ? "prev" : "reset"),
                  n.emit("transitionEnd"),
                  e && i !== r)
                ) {
                  if ("reset" === o)
                    return void n.emit("slideResetTransitionEnd");
                  n.emit("slideChangeTransitionEnd"),
                    "next" === o
                      ? n.emit("slideNextTransitionEnd")
                      : n.emit("slidePrevTransitionEnd");
                }
              }
            },
          },
          slide: S,
          loop: C,
          grabCursor: {
            setGrabCursor: function setGrabCursor(e) {
              var t = this;
              if (
                !(
                  t.support.touch ||
                  !t.params.simulateTouch ||
                  (t.params.watchOverflow && t.isLocked) ||
                  t.params.cssMode
                )
              ) {
                var n = t.el;
                (n.style.cursor = "move"),
                  (n.style.cursor = e ? "-webkit-grabbing" : "-webkit-grab"),
                  (n.style.cursor = e ? "-moz-grabbin" : "-moz-grab"),
                  (n.style.cursor = e ? "grabbing" : "grab");
              }
            },
            unsetGrabCursor: function unsetGrabCursor() {
              var e = this;
              e.support.touch ||
                (e.params.watchOverflow && e.isLocked) ||
                e.params.cssMode ||
                (e.el.style.cursor = "");
            },
          },
          manipulation: T,
          events: M,
          breakpoints: {
            setBreakpoint: function setBreakpoint() {
              var e = this,
                t = e.activeIndex,
                n = e.initialized,
                i = e.loopedSlides,
                r = void 0 === i ? 0 : i,
                a = e.params,
                o = e.$el,
                s = a.breakpoints;
              if (s && (!s || 0 !== Object.keys(s).length)) {
                var l = e.getBreakpoint(s, e.params.breakpointsBase, e.el);
                if (l && e.currentBreakpoint !== l) {
                  var c = l in s ? s[l] : void 0;
                  c &&
                    [
                      "slidesPerView",
                      "spaceBetween",
                      "slidesPerGroup",
                      "slidesPerGroupSkip",
                      "slidesPerColumn",
                    ].forEach(function (e) {
                      var t = c[e];
                      void 0 !== t &&
                        (c[e] =
                          "slidesPerView" !== e ||
                          ("AUTO" !== t && "auto" !== t)
                            ? "slidesPerView" === e
                              ? parseFloat(t)
                              : parseInt(t, 10)
                            : "auto");
                    });
                  var d = c || e.originalParams,
                    u = a.slidesPerColumn > 1,
                    p = d.slidesPerColumn > 1,
                    h = a.enabled;
                  u && !p
                    ? (o.removeClass(
                        a.containerModifierClass +
                          "multirow " +
                          a.containerModifierClass +
                          "multirow-column"
                      ),
                      e.emitContainerClasses())
                    : !u &&
                      p &&
                      (o.addClass(a.containerModifierClass + "multirow"),
                      "column" === d.slidesPerColumnFill &&
                        o.addClass(
                          a.containerModifierClass + "multirow-column"
                        ),
                      e.emitContainerClasses());
                  var m = d.direction && d.direction !== a.direction,
                    f = a.loop && (d.slidesPerView !== a.slidesPerView || m);
                  m && n && e.changeDirection(), extend(e.params, d);
                  var v = e.params.enabled;
                  extend(e, {
                    allowTouchMove: e.params.allowTouchMove,
                    allowSlideNext: e.params.allowSlideNext,
                    allowSlidePrev: e.params.allowSlidePrev,
                  }),
                    h && !v ? e.disable() : !h && v && e.enable(),
                    (e.currentBreakpoint = l),
                    e.emit("_beforeBreakpoint", d),
                    f &&
                      n &&
                      (e.loopDestroy(),
                      e.loopCreate(),
                      e.updateSlides(),
                      e.slideTo(t - r + e.loopedSlides, 0, !1)),
                    e.emit("breakpoint", d);
                }
              }
            },
            getBreakpoint: function getBreakpoint(e, t, n) {
              if (
                (void 0 === t && (t = "window"), e && ("container" !== t || n))
              ) {
                var i = !1,
                  r = getWindow(),
                  a = "window" === t ? r.innerHeight : n.clientHeight,
                  o = Object.keys(e).map(function (e) {
                    if ("string" == typeof e && 0 === e.indexOf("@")) {
                      var t = parseFloat(e.substr(1));
                      return {
                        value: a * t,
                        point: e,
                      };
                    }
                    return {
                      value: e,
                      point: e,
                    };
                  });
                o.sort(function (e, t) {
                  return parseInt(e.value, 10) - parseInt(t.value, 10);
                });
                for (var s = 0; s < o.length; s += 1) {
                  var l = o[s],
                    c = l.point,
                    d = l.value;
                  "window" === t
                    ? r.matchMedia("(min-width: " + d + "px)").matches &&
                      (i = c)
                    : d <= n.clientWidth && (i = c);
                }
                return i || "max";
              }
            },
          },
          checkOverflow: {
            checkOverflow: function checkOverflow() {
              var e = this,
                t = e.params,
                n = e.isLocked,
                i =
                  e.slides.length > 0 &&
                  t.slidesOffsetBefore +
                    t.spaceBetween * (e.slides.length - 1) +
                    e.slides[0].offsetWidth * e.slides.length;
              t.slidesOffsetBefore && t.slidesOffsetAfter && i
                ? (e.isLocked = i <= e.size)
                : (e.isLocked = 1 === e.snapGrid.length),
                (e.allowSlideNext = !e.isLocked),
                (e.allowSlidePrev = !e.isLocked),
                n !== e.isLocked && e.emit(e.isLocked ? "lock" : "unlock"),
                n &&
                  n !== e.isLocked &&
                  ((e.isEnd = !1), e.navigation && e.navigation.update());
            },
          },
          classes: I,
          images: {
            loadImage: function loadImage(e, t, n, i, r, a) {
              var o,
                s = getWindow();
              function onReady() {
                a && a();
              }
              $(e).parent("picture")[0] || (e.complete && r)
                ? onReady()
                : t
                ? (((o = new s.Image()).onload = onReady),
                  (o.onerror = onReady),
                  i && (o.sizes = i),
                  n && (o.srcset = n),
                  t && (o.src = t))
                : onReady();
            },
            preloadImages: function preloadImages() {
              var e = this;
              function onReady() {
                null != e &&
                  e &&
                  !e.destroyed &&
                  (void 0 !== e.imagesLoaded && (e.imagesLoaded += 1),
                  e.imagesLoaded === e.imagesToLoad.length &&
                    (e.params.updateOnImagesReady && e.update(),
                    e.emit("imagesReady")));
              }
              e.imagesToLoad = e.$el.find("img");
              for (var t = 0; t < e.imagesToLoad.length; t += 1) {
                var n = e.imagesToLoad[t];
                e.loadImage(
                  n,
                  n.currentSrc || n.getAttribute("src"),
                  n.srcset || n.getAttribute("srcset"),
                  n.sizes || n.getAttribute("sizes"),
                  !0,
                  onReady
                );
              }
            },
          },
        },
        N = {},
        O = (function () {
          function Swiper() {
            for (
              var e, t, n = arguments.length, i = new Array(n), r = 0;
              r < n;
              r++
            )
              i[r] = arguments[r];
            if (
              (1 === i.length &&
              i[0].constructor &&
              "Object" === Object.prototype.toString.call(i[0]).slice(8, -1)
                ? (t = i[0])
                : ((e = i[0]), (t = i[1])),
              t || (t = {}),
              (t = extend({}, t)),
              e && !t.el && (t.el = e),
              t.el && $(t.el).length > 1)
            ) {
              var a = [];
              return (
                $(t.el).each(function (e) {
                  var n = extend({}, t, {
                    el: e,
                  });
                  a.push(new Swiper(n));
                }),
                a
              );
            }
            var o = this;
            (o.__swiper__ = !0),
              (o.support = getSupport()),
              (o.device = getDevice({
                userAgent: t.userAgent,
              })),
              (o.browser = getBrowser()),
              (o.eventsListeners = {}),
              (o.eventsAnyListeners = []),
              void 0 === o.modules && (o.modules = {}),
              Object.keys(o.modules).forEach(function (e) {
                var n = o.modules[e];
                if (n.params) {
                  var i = Object.keys(n.params)[0],
                    r = n.params[i];
                  if ("object" !== (0, l.A)(r) || null === r) return;
                  if (
                    (["navigation", "pagination", "scrollbar"].indexOf(i) >=
                      0 &&
                      !0 === t[i] &&
                      (t[i] = {
                        auto: !0,
                      }),
                    !(i in t) || !("enabled" in r))
                  )
                    return;
                  !0 === t[i] &&
                    (t[i] = {
                      enabled: !0,
                    }),
                    "object" !== (0, l.A)(t[i]) ||
                      "enabled" in t[i] ||
                      (t[i].enabled = !0),
                    t[i] ||
                      (t[i] = {
                        enabled: !1,
                      });
                }
              });
            var s = extend({}, L);
            return (
              o.useParams(s),
              (o.params = extend({}, s, N, t)),
              (o.originalParams = extend({}, o.params)),
              (o.passedParams = extend({}, t)),
              o.params &&
                o.params.on &&
                Object.keys(o.params.on).forEach(function (e) {
                  o.on(e, o.params.on[e]);
                }),
              o.params && o.params.onAny && o.onAny(o.params.onAny),
              (o.$ = $),
              extend(o, {
                enabled: o.params.enabled,
                el: e,
                classNames: [],
                slides: $(),
                slidesGrid: [],
                snapGrid: [],
                slidesSizesGrid: [],
                isHorizontal: function isHorizontal() {
                  return "horizontal" === o.params.direction;
                },
                isVertical: function isVertical() {
                  return "vertical" === o.params.direction;
                },
                activeIndex: 0,
                realIndex: 0,
                isBeginning: !0,
                isEnd: !1,
                translate: 0,
                previousTranslate: 0,
                progress: 0,
                velocity: 0,
                animating: !1,
                allowSlideNext: o.params.allowSlideNext,
                allowSlidePrev: o.params.allowSlidePrev,
                touchEvents: (function touchEvents() {
                  var e = [
                      "touchstart",
                      "touchmove",
                      "touchend",
                      "touchcancel",
                    ],
                    t = ["mousedown", "mousemove", "mouseup"];
                  return (
                    o.support.pointerEvents &&
                      (t = ["pointerdown", "pointermove", "pointerup"]),
                    (o.touchEventsTouch = {
                      start: e[0],
                      move: e[1],
                      end: e[2],
                      cancel: e[3],
                    }),
                    (o.touchEventsDesktop = {
                      start: t[0],
                      move: t[1],
                      end: t[2],
                    }),
                    o.support.touch || !o.params.simulateTouch
                      ? o.touchEventsTouch
                      : o.touchEventsDesktop
                  );
                })(),
                touchEventsData: {
                  isTouched: void 0,
                  isMoved: void 0,
                  allowTouchCallbacks: void 0,
                  touchStartTime: void 0,
                  isScrolling: void 0,
                  currentTranslate: void 0,
                  startTranslate: void 0,
                  allowThresholdMove: void 0,
                  focusableElements: o.params.focusableElements,
                  lastClickTime: now(),
                  clickTimeout: void 0,
                  velocities: [],
                  allowMomentumBounce: void 0,
                  isTouchEvent: void 0,
                  startMoving: void 0,
                },
                allowClick: !0,
                allowTouchMove: o.params.allowTouchMove,
                touches: {
                  startX: 0,
                  startY: 0,
                  currentX: 0,
                  currentY: 0,
                  diff: 0,
                },
                imagesToLoad: [],
                imagesLoaded: 0,
              }),
              o.useModules(),
              o.emit("_swiper"),
              o.params.init && o.init(),
              o
            );
          }
          var e = Swiper.prototype;
          return (
            (e.enable = function enable() {
              var e = this;
              e.enabled ||
                ((e.enabled = !0),
                e.params.grabCursor && e.setGrabCursor(),
                e.emit("enable"));
            }),
            (e.disable = function disable() {
              var e = this;
              e.enabled &&
                ((e.enabled = !1),
                e.params.grabCursor && e.unsetGrabCursor(),
                e.emit("disable"));
            }),
            (e.setProgress = function setProgress(e, t) {
              var n = this;
              e = Math.min(Math.max(e, 0), 1);
              var i = n.minTranslate(),
                r = (n.maxTranslate() - i) * e + i;
              n.translateTo(r, void 0 === t ? 0 : t),
                n.updateActiveIndex(),
                n.updateSlidesClasses();
            }),
            (e.emitContainerClasses = function emitContainerClasses() {
              var e = this;
              if (e.params._emitClasses && e.el) {
                var t = e.el.className.split(" ").filter(function (t) {
                  return (
                    0 === t.indexOf("swiper-container") ||
                    0 === t.indexOf(e.params.containerModifierClass)
                  );
                });
                e.emit("_containerClasses", t.join(" "));
              }
            }),
            (e.getSlideClasses = function getSlideClasses(e) {
              var t = this;
              return e.className
                .split(" ")
                .filter(function (e) {
                  return (
                    0 === e.indexOf("swiper-slide") ||
                    0 === e.indexOf(t.params.slideClass)
                  );
                })
                .join(" ");
            }),
            (e.emitSlidesClasses = function emitSlidesClasses() {
              var e = this;
              if (e.params._emitClasses && e.el) {
                var t = [];
                e.slides.each(function (n) {
                  var i = e.getSlideClasses(n);
                  t.push({
                    slideEl: n,
                    classNames: i,
                  }),
                    e.emit("_slideClass", n, i);
                }),
                  e.emit("_slideClasses", t);
              }
            }),
            (e.slidesPerViewDynamic = function slidesPerViewDynamic() {
              var e = this,
                t = e.params,
                n = e.slides,
                i = e.slidesGrid,
                r = e.size,
                a = e.activeIndex,
                o = 1;
              if (t.centeredSlides) {
                for (
                  var s, l = n[a].swiperSlideSize, c = a + 1;
                  c < n.length;
                  c += 1
                )
                  n[c] &&
                    !s &&
                    ((o += 1), (l += n[c].swiperSlideSize) > r && (s = !0));
                for (var d = a - 1; d >= 0; d -= 1)
                  n[d] &&
                    !s &&
                    ((o += 1), (l += n[d].swiperSlideSize) > r && (s = !0));
              } else
                for (var u = a + 1; u < n.length; u += 1)
                  i[u] - i[a] < r && (o += 1);
              return o;
            }),
            (e.update = function update() {
              var e = this;
              if (e && !e.destroyed) {
                var t = e.snapGrid,
                  n = e.params;
                n.breakpoints && e.setBreakpoint(),
                  e.updateSize(),
                  e.updateSlides(),
                  e.updateProgress(),
                  e.updateSlidesClasses(),
                  e.params.freeMode
                    ? (setTranslate(),
                      e.params.autoHeight && e.updateAutoHeight())
                    : (("auto" === e.params.slidesPerView ||
                        e.params.slidesPerView > 1) &&
                      e.isEnd &&
                      !e.params.centeredSlides
                        ? e.slideTo(e.slides.length - 1, 0, !1, !0)
                        : e.slideTo(e.activeIndex, 0, !1, !0)) ||
                      setTranslate(),
                  n.watchOverflow && t !== e.snapGrid && e.checkOverflow(),
                  e.emit("update");
              }
              function setTranslate() {
                var t = e.rtlTranslate ? -1 * e.translate : e.translate,
                  n = Math.min(Math.max(t, e.maxTranslate()), e.minTranslate());
                e.setTranslate(n),
                  e.updateActiveIndex(),
                  e.updateSlidesClasses();
              }
            }),
            (e.changeDirection = function changeDirection(e, t) {
              void 0 === t && (t = !0);
              var n = this,
                i = n.params.direction;
              return (
                e || (e = "horizontal" === i ? "vertical" : "horizontal"),
                e === i ||
                  ("horizontal" !== e && "vertical" !== e) ||
                  (n.$el
                    .removeClass("" + n.params.containerModifierClass + i)
                    .addClass("" + n.params.containerModifierClass + e),
                  n.emitContainerClasses(),
                  (n.params.direction = e),
                  n.slides.each(function (t) {
                    "vertical" === e
                      ? (t.style.width = "")
                      : (t.style.height = "");
                  }),
                  n.emit("changeDirection"),
                  t && n.update()),
                n
              );
            }),
            (e.mount = function mount(e) {
              var t = this;
              if (t.mounted) return !0;
              var n = $(e || t.params.el);
              if (!(e = n[0])) return !1;
              e.swiper = t;
              var i = function getWrapperSelector() {
                  return (
                    "." +
                    (t.params.wrapperClass || "").trim().split(" ").join(".")
                  );
                },
                r = (function getWrapper() {
                  if (e && e.shadowRoot && e.shadowRoot.querySelector) {
                    var t = $(e.shadowRoot.querySelector(i()));
                    return (
                      (t.children = function (e) {
                        return n.children(e);
                      }),
                      t
                    );
                  }
                  return n.children(i());
                })();
              if (0 === r.length && t.params.createElements) {
                var a = getDocument().createElement("div");
                (r = $(a)),
                  (a.className = t.params.wrapperClass),
                  n.append(a),
                  n.children("." + t.params.slideClass).each(function (e) {
                    r.append(e);
                  });
              }
              return (
                extend(t, {
                  $el: n,
                  el: e,
                  $wrapperEl: r,
                  wrapperEl: r[0],
                  mounted: !0,
                  rtl:
                    "rtl" === e.dir.toLowerCase() ||
                    "rtl" === n.css("direction"),
                  rtlTranslate:
                    "horizontal" === t.params.direction &&
                    ("rtl" === e.dir.toLowerCase() ||
                      "rtl" === n.css("direction")),
                  wrongRTL: "-webkit-box" === r.css("display"),
                }),
                !0
              );
            }),
            (e.init = function init(e) {
              var t = this;
              return (
                t.initialized ||
                  !1 === t.mount(e) ||
                  (t.emit("beforeInit"),
                  t.params.breakpoints && t.setBreakpoint(),
                  t.addClasses(),
                  t.params.loop && t.loopCreate(),
                  t.updateSize(),
                  t.updateSlides(),
                  t.params.watchOverflow && t.checkOverflow(),
                  t.params.grabCursor && t.enabled && t.setGrabCursor(),
                  t.params.preloadImages && t.preloadImages(),
                  t.params.loop
                    ? t.slideTo(
                        t.params.initialSlide + t.loopedSlides,
                        0,
                        t.params.runCallbacksOnInit,
                        !1,
                        !0
                      )
                    : t.slideTo(
                        t.params.initialSlide,
                        0,
                        t.params.runCallbacksOnInit,
                        !1,
                        !0
                      ),
                  t.attachEvents(),
                  (t.initialized = !0),
                  t.emit("init"),
                  t.emit("afterInit")),
                t
              );
            }),
            (e.destroy = function destroy(e, t) {
              void 0 === e && (e = !0), void 0 === t && (t = !0);
              var n = this,
                i = n.params,
                r = n.$el,
                a = n.$wrapperEl,
                o = n.slides;
              return (
                void 0 === n.params ||
                  n.destroyed ||
                  (n.emit("beforeDestroy"),
                  (n.initialized = !1),
                  n.detachEvents(),
                  i.loop && n.loopDestroy(),
                  t &&
                    (n.removeClasses(),
                    r.removeAttr("style"),
                    a.removeAttr("style"),
                    o &&
                      o.length &&
                      o
                        .removeClass(
                          [
                            i.slideVisibleClass,
                            i.slideActiveClass,
                            i.slideNextClass,
                            i.slidePrevClass,
                          ].join(" ")
                        )
                        .removeAttr("style")
                        .removeAttr("data-swiper-slide-index")),
                  n.emit("destroy"),
                  Object.keys(n.eventsListeners).forEach(function (e) {
                    n.off(e);
                  }),
                  !1 !== e &&
                    ((n.$el[0].swiper = null),
                    (function deleteProps(e) {
                      var t = e;
                      Object.keys(t).forEach(function (e) {
                        try {
                          t[e] = null;
                        } catch (e) {}
                        try {
                          delete t[e];
                        } catch (e) {}
                      });
                    })(n)),
                  (n.destroyed = !0)),
                null
              );
            }),
            (Swiper.extendDefaults = function extendDefaults(e) {
              extend(N, e);
            }),
            (Swiper.installModule = function installModule(e) {
              Swiper.prototype.modules || (Swiper.prototype.modules = {});
              var t =
                e.name ||
                Object.keys(Swiper.prototype.modules).length + "_" + now();
              Swiper.prototype.modules[t] = e;
            }),
            (Swiper.use = function use(e) {
              return Array.isArray(e)
                ? (e.forEach(function (e) {
                    return Swiper.installModule(e);
                  }),
                  Swiper)
                : (Swiper.installModule(e), Swiper);
            }),
            (function _createClass(e, t, n) {
              return (
                t && _defineProperties(e.prototype, t),
                n && _defineProperties(e, n),
                e
              );
            })(Swiper, null, [
              {
                key: "extendedDefaults",
                get: function get() {
                  return N;
                },
              },
              {
                key: "defaults",
                get: function get() {
                  return L;
                },
              },
            ]),
            Swiper
          );
        })();
      function _extends$h() {
        return (
          (_extends$h =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$h.apply(this, arguments)
        );
      }
      Object.keys(P).forEach(function (e) {
        Object.keys(P[e]).forEach(function (t) {
          O.prototype[t] = P[e][t];
        });
      }),
        O.use([b, A]);
      var D = {
          update: function update(e) {
            var t = this,
              n = t.params,
              i = n.slidesPerView,
              r = n.slidesPerGroup,
              a = n.centeredSlides,
              o = t.params.virtual,
              s = o.addSlidesBefore,
              l = o.addSlidesAfter,
              c = t.virtual,
              d = c.from,
              u = c.to,
              p = c.slides,
              h = c.slidesGrid,
              m = c.renderSlide,
              f = c.offset;
            t.updateActiveIndex();
            var v,
              g,
              w,
              b = t.activeIndex || 0;
            (v = t.rtlTranslate ? "right" : t.isHorizontal() ? "left" : "top"),
              a
                ? ((g = Math.floor(i / 2) + r + l),
                  (w = Math.floor(i / 2) + r + s))
                : ((g = i + (r - 1) + l), (w = r + s));
            var y = Math.max((b || 0) - w, 0),
              A = Math.min((b || 0) + g, p.length - 1),
              x = (t.slidesGrid[y] || 0) - (t.slidesGrid[0] || 0);
            function onRendered() {
              t.updateSlides(),
                t.updateProgress(),
                t.updateSlidesClasses(),
                t.lazy && t.params.lazy.enabled && t.lazy.load();
            }
            if (
              (extend(t.virtual, {
                from: y,
                to: A,
                offset: x,
                slidesGrid: t.slidesGrid,
              }),
              d === y && u === A && !e)
            )
              return (
                t.slidesGrid !== h && x !== f && t.slides.css(v, x + "px"),
                void t.updateProgress()
              );
            if (t.params.virtual.renderExternal)
              return (
                t.params.virtual.renderExternal.call(t, {
                  offset: x,
                  from: y,
                  to: A,
                  slides: (function getSlides() {
                    for (var e = [], t = y; t <= A; t += 1) e.push(p[t]);
                    return e;
                  })(),
                }),
                void (t.params.virtual.renderExternalUpdate && onRendered())
              );
            var _ = [],
              E = [];
            if (e) t.$wrapperEl.find("." + t.params.slideClass).remove();
            else
              for (var S = d; S <= u; S += 1)
                (S < y || S > A) &&
                  t.$wrapperEl
                    .find(
                      "." +
                        t.params.slideClass +
                        '[data-swiper-slide-index="' +
                        S +
                        '"]'
                    )
                    .remove();
            for (var C = 0; C < p.length; C += 1)
              C >= y &&
                C <= A &&
                (void 0 === u || e
                  ? E.push(C)
                  : (C > u && E.push(C), C < d && _.push(C)));
            E.forEach(function (e) {
              t.$wrapperEl.append(m(p[e], e));
            }),
              _.sort(function (e, t) {
                return t - e;
              }).forEach(function (e) {
                t.$wrapperEl.prepend(m(p[e], e));
              }),
              t.$wrapperEl.children(".swiper-slide").css(v, x + "px"),
              onRendered();
          },
          renderSlide: function renderSlide(e, t) {
            var n = this,
              i = n.params.virtual;
            if (i.cache && n.virtual.cache[t]) return n.virtual.cache[t];
            var r = i.renderSlide
              ? $(i.renderSlide.call(n, e, t))
              : $(
                  '<div class="' +
                    n.params.slideClass +
                    '" data-swiper-slide-index="' +
                    t +
                    '">' +
                    e +
                    "</div>"
                );
            return (
              r.attr("data-swiper-slide-index") ||
                r.attr("data-swiper-slide-index", t),
              i.cache && (n.virtual.cache[t] = r),
              r
            );
          },
          appendSlide: function appendSlide(e) {
            var t = this;
            if ("object" === (0, l.A)(e) && "length" in e)
              for (var n = 0; n < e.length; n += 1)
                e[n] && t.virtual.slides.push(e[n]);
            else t.virtual.slides.push(e);
            t.virtual.update(!0);
          },
          prependSlide: function prependSlide(e) {
            var t = this,
              n = t.activeIndex,
              i = n + 1,
              r = 1;
            if (Array.isArray(e)) {
              for (var a = 0; a < e.length; a += 1)
                e[a] && t.virtual.slides.unshift(e[a]);
              (i = n + e.length), (r = e.length);
            } else t.virtual.slides.unshift(e);
            if (t.params.virtual.cache) {
              var o = t.virtual.cache,
                s = {};
              Object.keys(o).forEach(function (e) {
                var t = o[e],
                  n = t.attr("data-swiper-slide-index");
                n && t.attr("data-swiper-slide-index", parseInt(n, 10) + 1),
                  (s[parseInt(e, 10) + r] = t);
              }),
                (t.virtual.cache = s);
            }
            t.virtual.update(!0), t.slideTo(i, 0);
          },
          removeSlide: function removeSlide(e) {
            var t = this;
            if (null != e) {
              var n = t.activeIndex;
              if (Array.isArray(e))
                for (var i = e.length - 1; i >= 0; i -= 1)
                  t.virtual.slides.splice(e[i], 1),
                    t.params.virtual.cache && delete t.virtual.cache[e[i]],
                    e[i] < n && (n -= 1),
                    (n = Math.max(n, 0));
              else
                t.virtual.slides.splice(e, 1),
                  t.params.virtual.cache && delete t.virtual.cache[e],
                  e < n && (n -= 1),
                  (n = Math.max(n, 0));
              t.virtual.update(!0), t.slideTo(n, 0);
            }
          },
          removeAllSlides: function removeAllSlides() {
            var e = this;
            (e.virtual.slides = []),
              e.params.virtual.cache && (e.virtual.cache = {}),
              e.virtual.update(!0),
              e.slideTo(0, 0);
          },
        },
        z = {
          name: "virtual",
          params: {
            virtual: {
              enabled: !1,
              slides: [],
              cache: !0,
              renderSlide: null,
              renderExternal: null,
              renderExternalUpdate: !0,
              addSlidesBefore: 0,
              addSlidesAfter: 0,
            },
          },
          create: function create() {
            bindModuleMethods(this, {
              virtual: _extends$h({}, D, {
                slides: this.params.virtual.slides,
                cache: {},
              }),
            });
          },
          on: {
            beforeInit: function beforeInit(e) {
              if (e.params.virtual.enabled) {
                e.classNames.push(e.params.containerModifierClass + "virtual");
                var t = {
                  watchSlidesProgress: !0,
                };
                extend(e.params, t),
                  extend(e.originalParams, t),
                  e.params.initialSlide || e.virtual.update();
              }
            },
            setTranslate: function setTranslate(e) {
              e.params.virtual.enabled && e.virtual.update();
            },
          },
        };
      function _extends$g() {
        return (
          (_extends$g =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$g.apply(this, arguments)
        );
      }
      var j = {
          handle: function handle(e) {
            var t = this;
            if (t.enabled) {
              var n = getWindow(),
                i = getDocument(),
                r = t.rtlTranslate,
                a = e;
              a.originalEvent && (a = a.originalEvent);
              var o = a.keyCode || a.charCode,
                s = t.params.keyboard.pageUpDown,
                l = s && 33 === o,
                c = s && 34 === o,
                d = 37 === o,
                u = 39 === o,
                p = 38 === o,
                h = 40 === o;
              if (
                !t.allowSlideNext &&
                ((t.isHorizontal() && u) || (t.isVertical() && h) || c)
              )
                return !1;
              if (
                !t.allowSlidePrev &&
                ((t.isHorizontal() && d) || (t.isVertical() && p) || l)
              )
                return !1;
              if (
                !(
                  a.shiftKey ||
                  a.altKey ||
                  a.ctrlKey ||
                  a.metaKey ||
                  (i.activeElement &&
                    i.activeElement.nodeName &&
                    ("input" === i.activeElement.nodeName.toLowerCase() ||
                      "textarea" === i.activeElement.nodeName.toLowerCase()))
                )
              ) {
                if (
                  t.params.keyboard.onlyInViewport &&
                  (l || c || d || u || p || h)
                ) {
                  var m = !1;
                  if (
                    t.$el.parents("." + t.params.slideClass).length > 0 &&
                    0 === t.$el.parents("." + t.params.slideActiveClass).length
                  )
                    return;
                  var f = t.$el,
                    v = f[0].clientWidth,
                    g = f[0].clientHeight,
                    w = n.innerWidth,
                    b = n.innerHeight,
                    y = t.$el.offset();
                  r && (y.left -= t.$el[0].scrollLeft);
                  for (
                    var A = [
                        [y.left, y.top],
                        [y.left + v, y.top],
                        [y.left, y.top + g],
                        [y.left + v, y.top + g],
                      ],
                      x = 0;
                    x < A.length;
                    x += 1
                  ) {
                    var _ = A[x];
                    if (_[0] >= 0 && _[0] <= w && _[1] >= 0 && _[1] <= b) {
                      if (0 === _[0] && 0 === _[1]) continue;
                      m = !0;
                    }
                  }
                  if (!m) return;
                }
                t.isHorizontal()
                  ? ((l || c || d || u) &&
                      (a.preventDefault
                        ? a.preventDefault()
                        : (a.returnValue = !1)),
                    (((c || u) && !r) || ((l || d) && r)) && t.slideNext(),
                    (((l || d) && !r) || ((c || u) && r)) && t.slidePrev())
                  : ((l || c || p || h) &&
                      (a.preventDefault
                        ? a.preventDefault()
                        : (a.returnValue = !1)),
                    (c || h) && t.slideNext(),
                    (l || p) && t.slidePrev()),
                  t.emit("keyPress", o);
              }
            }
          },
          enable: function enable() {
            var e = this,
              t = getDocument();
            e.keyboard.enabled ||
              ($(t).on("keydown", e.keyboard.handle),
              (e.keyboard.enabled = !0));
          },
          disable: function disable() {
            var e = this,
              t = getDocument();
            e.keyboard.enabled &&
              ($(t).off("keydown", e.keyboard.handle),
              (e.keyboard.enabled = !1));
          },
        },
        B = {
          name: "keyboard",
          params: {
            keyboard: {
              enabled: !1,
              onlyInViewport: !0,
              pageUpDown: !0,
            },
          },
          create: function create() {
            bindModuleMethods(this, {
              keyboard: _extends$g(
                {
                  enabled: !1,
                },
                j
              ),
            });
          },
          on: {
            init: function init(e) {
              e.params.keyboard.enabled && e.keyboard.enable();
            },
            destroy: function destroy(e) {
              e.keyboard.enabled && e.keyboard.disable();
            },
          },
        };
      var R = {
        lastScrollTime: now(),
        lastEventBeforeSnap: void 0,
        recentWheelEvents: [],
        event: function event() {
          return getWindow().navigator.userAgent.indexOf("firefox") > -1
            ? "DOMMouseScroll"
            : (function isEventSupported() {
                var e = getDocument(),
                  t = "onwheel",
                  n = t in e;
                if (!n) {
                  var i = e.createElement("div");
                  i.setAttribute(t, "return;"), (n = "function" == typeof i[t]);
                }
                return (
                  !n &&
                    e.implementation &&
                    e.implementation.hasFeature &&
                    !0 !== e.implementation.hasFeature("", "") &&
                    (n = e.implementation.hasFeature("Events.wheel", "3.0")),
                  n
                );
              })()
            ? "wheel"
            : "mousewheel";
        },
        normalize: function normalize(e) {
          var t = 0,
            n = 0,
            i = 0,
            r = 0;
          return (
            "detail" in e && (n = e.detail),
            "wheelDelta" in e && (n = -e.wheelDelta / 120),
            "wheelDeltaY" in e && (n = -e.wheelDeltaY / 120),
            "wheelDeltaX" in e && (t = -e.wheelDeltaX / 120),
            "axis" in e && e.axis === e.HORIZONTAL_AXIS && ((t = n), (n = 0)),
            (i = 10 * t),
            (r = 10 * n),
            "deltaY" in e && (r = e.deltaY),
            "deltaX" in e && (i = e.deltaX),
            e.shiftKey && !i && ((i = r), (r = 0)),
            (i || r) &&
              e.deltaMode &&
              (1 === e.deltaMode
                ? ((i *= 40), (r *= 40))
                : ((i *= 800), (r *= 800))),
            i && !t && (t = i < 1 ? -1 : 1),
            r && !n && (n = r < 1 ? -1 : 1),
            {
              spinX: t,
              spinY: n,
              pixelX: i,
              pixelY: r,
            }
          );
        },
        handleMouseEnter: function handleMouseEnter() {
          this.enabled && (this.mouseEntered = !0);
        },
        handleMouseLeave: function handleMouseLeave() {
          this.enabled && (this.mouseEntered = !1);
        },
        handle: function handle(e) {
          var t = e,
            n = this;
          if (n.enabled) {
            var i = n.params.mousewheel;
            n.params.cssMode && t.preventDefault();
            var r = n.$el;
            if (
              ("container" !== n.params.mousewheel.eventsTarget &&
                (r = $(n.params.mousewheel.eventsTarget)),
              !n.mouseEntered && !r[0].contains(t.target) && !i.releaseOnEdges)
            )
              return !0;
            t.originalEvent && (t = t.originalEvent);
            var a = 0,
              o = n.rtlTranslate ? -1 : 1,
              s = R.normalize(t);
            if (i.forceToAxis)
              if (n.isHorizontal()) {
                if (!(Math.abs(s.pixelX) > Math.abs(s.pixelY))) return !0;
                a = -s.pixelX * o;
              } else {
                if (!(Math.abs(s.pixelY) > Math.abs(s.pixelX))) return !0;
                a = -s.pixelY;
              }
            else
              a =
                Math.abs(s.pixelX) > Math.abs(s.pixelY)
                  ? -s.pixelX * o
                  : -s.pixelY;
            if (0 === a) return !0;
            i.invert && (a = -a);
            var l = n.getTranslate() + a * i.sensitivity;
            if (
              (l >= n.minTranslate() && (l = n.minTranslate()),
              l <= n.maxTranslate() && (l = n.maxTranslate()),
              (!!n.params.loop ||
                !(l === n.minTranslate() || l === n.maxTranslate())) &&
                n.params.nested &&
                t.stopPropagation(),
              n.params.freeMode)
            ) {
              var c = {
                  time: now(),
                  delta: Math.abs(a),
                  direction: Math.sign(a),
                },
                d = n.mousewheel.lastEventBeforeSnap,
                u =
                  d &&
                  c.time < d.time + 500 &&
                  c.delta <= d.delta &&
                  c.direction === d.direction;
              if (!u) {
                (n.mousewheel.lastEventBeforeSnap = void 0),
                  n.params.loop && n.loopFix();
                var p = n.getTranslate() + a * i.sensitivity,
                  h = n.isBeginning,
                  m = n.isEnd;
                if (
                  (p >= n.minTranslate() && (p = n.minTranslate()),
                  p <= n.maxTranslate() && (p = n.maxTranslate()),
                  n.setTransition(0),
                  n.setTranslate(p),
                  n.updateProgress(),
                  n.updateActiveIndex(),
                  n.updateSlidesClasses(),
                  ((!h && n.isBeginning) || (!m && n.isEnd)) &&
                    n.updateSlidesClasses(),
                  n.params.freeModeSticky)
                ) {
                  clearTimeout(n.mousewheel.timeout),
                    (n.mousewheel.timeout = void 0);
                  var f = n.mousewheel.recentWheelEvents;
                  f.length >= 15 && f.shift();
                  var v = f.length ? f[f.length - 1] : void 0,
                    g = f[0];
                  if (
                    (f.push(c),
                    v && (c.delta > v.delta || c.direction !== v.direction))
                  )
                    f.splice(0);
                  else if (
                    f.length >= 15 &&
                    c.time - g.time < 500 &&
                    g.delta - c.delta >= 1 &&
                    c.delta <= 6
                  ) {
                    var w = a > 0 ? 0.8 : 0.2;
                    (n.mousewheel.lastEventBeforeSnap = c),
                      f.splice(0),
                      (n.mousewheel.timeout = nextTick(function () {
                        n.slideToClosest(n.params.speed, !0, void 0, w);
                      }, 0));
                  }
                  n.mousewheel.timeout ||
                    (n.mousewheel.timeout = nextTick(function () {
                      (n.mousewheel.lastEventBeforeSnap = c),
                        f.splice(0),
                        n.slideToClosest(n.params.speed, !0, void 0, 0.5);
                    }, 500));
                }
                if (
                  (u || n.emit("scroll", t),
                  n.params.autoplay &&
                    n.params.autoplayDisableOnInteraction &&
                    n.autoplay.stop(),
                  p === n.minTranslate() || p === n.maxTranslate())
                )
                  return !0;
              }
            } else {
              var b = {
                  time: now(),
                  delta: Math.abs(a),
                  direction: Math.sign(a),
                  raw: e,
                },
                y = n.mousewheel.recentWheelEvents;
              y.length >= 2 && y.shift();
              var A = y.length ? y[y.length - 1] : void 0;
              if (
                (y.push(b),
                A
                  ? (b.direction !== A.direction ||
                      b.delta > A.delta ||
                      b.time > A.time + 150) &&
                    n.mousewheel.animateSlider(b)
                  : n.mousewheel.animateSlider(b),
                n.mousewheel.releaseScroll(b))
              )
                return !0;
            }
            return (
              t.preventDefault ? t.preventDefault() : (t.returnValue = !1), !1
            );
          }
        },
        animateSlider: function animateSlider(e) {
          var t = this,
            n = getWindow();
          return (
            !(
              this.params.mousewheel.thresholdDelta &&
              e.delta < this.params.mousewheel.thresholdDelta
            ) &&
            !(
              this.params.mousewheel.thresholdTime &&
              now() - t.mousewheel.lastScrollTime <
                this.params.mousewheel.thresholdTime
            ) &&
            ((e.delta >= 6 && now() - t.mousewheel.lastScrollTime < 60) ||
              (e.direction < 0
                ? (t.isEnd && !t.params.loop) ||
                  t.animating ||
                  (t.slideNext(), t.emit("scroll", e.raw))
                : (t.isBeginning && !t.params.loop) ||
                  t.animating ||
                  (t.slidePrev(), t.emit("scroll", e.raw)),
              (t.mousewheel.lastScrollTime = new n.Date().getTime()),
              !1))
          );
        },
        releaseScroll: function releaseScroll(e) {
          var t = this,
            n = t.params.mousewheel;
          if (e.direction < 0) {
            if (t.isEnd && !t.params.loop && n.releaseOnEdges) return !0;
          } else if (t.isBeginning && !t.params.loop && n.releaseOnEdges)
            return !0;
          return !1;
        },
        enable: function enable() {
          var e = this,
            t = R.event();
          if (e.params.cssMode)
            return e.wrapperEl.removeEventListener(t, e.mousewheel.handle), !0;
          if (!t) return !1;
          if (e.mousewheel.enabled) return !1;
          var n = e.$el;
          return (
            "container" !== e.params.mousewheel.eventsTarget &&
              (n = $(e.params.mousewheel.eventsTarget)),
            n.on("mouseenter", e.mousewheel.handleMouseEnter),
            n.on("mouseleave", e.mousewheel.handleMouseLeave),
            n.on(t, e.mousewheel.handle),
            (e.mousewheel.enabled = !0),
            !0
          );
        },
        disable: function disable() {
          var e = this,
            t = R.event();
          if (e.params.cssMode)
            return e.wrapperEl.addEventListener(t, e.mousewheel.handle), !0;
          if (!t) return !1;
          if (!e.mousewheel.enabled) return !1;
          var n = e.$el;
          return (
            "container" !== e.params.mousewheel.eventsTarget &&
              (n = $(e.params.mousewheel.eventsTarget)),
            n.off(t, e.mousewheel.handle),
            (e.mousewheel.enabled = !1),
            !0
          );
        },
      };
      function _extends$f() {
        return (
          (_extends$f =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$f.apply(this, arguments)
        );
      }
      var F = {
        toggleEl: function toggleEl(e, t) {
          e[t ? "addClass" : "removeClass"](
            this.params.navigation.disabledClass
          ),
            e[0] && "BUTTON" === e[0].tagName && (e[0].disabled = t);
        },
        update: function update() {
          var e = this,
            t = e.params.navigation,
            n = e.navigation.toggleEl;
          if (!e.params.loop) {
            var i = e.navigation,
              r = i.$nextEl,
              a = i.$prevEl;
            a &&
              a.length > 0 &&
              (e.isBeginning ? n(a, !0) : n(a, !1),
              e.params.watchOverflow &&
                e.enabled &&
                a[e.isLocked ? "addClass" : "removeClass"](t.lockClass)),
              r &&
                r.length > 0 &&
                (e.isEnd ? n(r, !0) : n(r, !1),
                e.params.watchOverflow &&
                  e.enabled &&
                  r[e.isLocked ? "addClass" : "removeClass"](t.lockClass));
          }
        },
        onPrevClick: function onPrevClick(e) {
          var t = this;
          e.preventDefault(),
            (t.isBeginning && !t.params.loop) || t.slidePrev();
        },
        onNextClick: function onNextClick(e) {
          var t = this;
          e.preventDefault(), (t.isEnd && !t.params.loop) || t.slideNext();
        },
        init: function init() {
          var e,
            t,
            n = this,
            i = n.params.navigation;
          ((n.params.navigation = createElementIfNotDefined(
            n.$el,
            n.params.navigation,
            n.params.createElements,
            {
              nextEl: "swiper-button-next",
              prevEl: "swiper-button-prev",
            }
          )),
          i.nextEl || i.prevEl) &&
            (i.nextEl &&
              ((e = $(i.nextEl)),
              n.params.uniqueNavElements &&
                "string" == typeof i.nextEl &&
                e.length > 1 &&
                1 === n.$el.find(i.nextEl).length &&
                (e = n.$el.find(i.nextEl))),
            i.prevEl &&
              ((t = $(i.prevEl)),
              n.params.uniqueNavElements &&
                "string" == typeof i.prevEl &&
                t.length > 1 &&
                1 === n.$el.find(i.prevEl).length &&
                (t = n.$el.find(i.prevEl))),
            e && e.length > 0 && e.on("click", n.navigation.onNextClick),
            t && t.length > 0 && t.on("click", n.navigation.onPrevClick),
            extend(n.navigation, {
              $nextEl: e,
              nextEl: e && e[0],
              $prevEl: t,
              prevEl: t && t[0],
            }),
            n.enabled ||
              (e && e.addClass(i.lockClass), t && t.addClass(i.lockClass)));
        },
        destroy: function destroy() {
          var e = this,
            t = e.navigation,
            n = t.$nextEl,
            i = t.$prevEl;
          n &&
            n.length &&
            (n.off("click", e.navigation.onNextClick),
            n.removeClass(e.params.navigation.disabledClass)),
            i &&
              i.length &&
              (i.off("click", e.navigation.onPrevClick),
              i.removeClass(e.params.navigation.disabledClass));
        },
      };
      function _extends$e() {
        return (
          (_extends$e =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$e.apply(this, arguments)
        );
      }
      var Y = {
        update: function update() {
          var e = this,
            t = e.rtl,
            n = e.params.pagination;
          if (
            n.el &&
            e.pagination.el &&
            e.pagination.$el &&
            0 !== e.pagination.$el.length
          ) {
            var i,
              r =
                e.virtual && e.params.virtual.enabled
                  ? e.virtual.slides.length
                  : e.slides.length,
              a = e.pagination.$el,
              o = e.params.loop
                ? Math.ceil((r - 2 * e.loopedSlides) / e.params.slidesPerGroup)
                : e.snapGrid.length;
            if (
              (e.params.loop
                ? ((i = Math.ceil(
                    (e.activeIndex - e.loopedSlides) / e.params.slidesPerGroup
                  )) >
                    r - 1 - 2 * e.loopedSlides && (i -= r - 2 * e.loopedSlides),
                  i > o - 1 && (i -= o),
                  i < 0 && "bullets" !== e.params.paginationType && (i = o + i))
                : (i =
                    void 0 !== e.snapIndex ? e.snapIndex : e.activeIndex || 0),
              "bullets" === n.type &&
                e.pagination.bullets &&
                e.pagination.bullets.length > 0)
            ) {
              var s,
                l,
                c,
                d = e.pagination.bullets;
              if (
                (n.dynamicBullets &&
                  ((e.pagination.bulletSize = d
                    .eq(0)
                    [e.isHorizontal() ? "outerWidth" : "outerHeight"](!0)),
                  a.css(
                    e.isHorizontal() ? "width" : "height",
                    e.pagination.bulletSize * (n.dynamicMainBullets + 4) + "px"
                  ),
                  n.dynamicMainBullets > 1 &&
                    void 0 !== e.previousIndex &&
                    ((e.pagination.dynamicBulletIndex += i - e.previousIndex),
                    e.pagination.dynamicBulletIndex > n.dynamicMainBullets - 1
                      ? (e.pagination.dynamicBulletIndex =
                          n.dynamicMainBullets - 1)
                      : e.pagination.dynamicBulletIndex < 0 &&
                        (e.pagination.dynamicBulletIndex = 0)),
                  (s = i - e.pagination.dynamicBulletIndex),
                  (c =
                    ((l = s + (Math.min(d.length, n.dynamicMainBullets) - 1)) +
                      s) /
                    2)),
                d.removeClass(
                  n.bulletActiveClass +
                    " " +
                    n.bulletActiveClass +
                    "-next " +
                    n.bulletActiveClass +
                    "-next-next " +
                    n.bulletActiveClass +
                    "-prev " +
                    n.bulletActiveClass +
                    "-prev-prev " +
                    n.bulletActiveClass +
                    "-main"
                ),
                a.length > 1)
              )
                d.each(function (e) {
                  var t = $(e),
                    r = t.index();
                  r === i && t.addClass(n.bulletActiveClass),
                    n.dynamicBullets &&
                      (r >= s &&
                        r <= l &&
                        t.addClass(n.bulletActiveClass + "-main"),
                      r === s &&
                        t
                          .prev()
                          .addClass(n.bulletActiveClass + "-prev")
                          .prev()
                          .addClass(n.bulletActiveClass + "-prev-prev"),
                      r === l &&
                        t
                          .next()
                          .addClass(n.bulletActiveClass + "-next")
                          .next()
                          .addClass(n.bulletActiveClass + "-next-next"));
                });
              else {
                var u = d.eq(i),
                  p = u.index();
                if ((u.addClass(n.bulletActiveClass), n.dynamicBullets)) {
                  for (var h = d.eq(s), m = d.eq(l), f = s; f <= l; f += 1)
                    d.eq(f).addClass(n.bulletActiveClass + "-main");
                  if (e.params.loop)
                    if (p >= d.length - n.dynamicMainBullets) {
                      for (var v = n.dynamicMainBullets; v >= 0; v -= 1)
                        d.eq(d.length - v).addClass(
                          n.bulletActiveClass + "-main"
                        );
                      d.eq(d.length - n.dynamicMainBullets - 1).addClass(
                        n.bulletActiveClass + "-prev"
                      );
                    } else
                      h
                        .prev()
                        .addClass(n.bulletActiveClass + "-prev")
                        .prev()
                        .addClass(n.bulletActiveClass + "-prev-prev"),
                        m
                          .next()
                          .addClass(n.bulletActiveClass + "-next")
                          .next()
                          .addClass(n.bulletActiveClass + "-next-next");
                  else
                    h
                      .prev()
                      .addClass(n.bulletActiveClass + "-prev")
                      .prev()
                      .addClass(n.bulletActiveClass + "-prev-prev"),
                      m
                        .next()
                        .addClass(n.bulletActiveClass + "-next")
                        .next()
                        .addClass(n.bulletActiveClass + "-next-next");
                }
              }
              if (n.dynamicBullets) {
                var g = Math.min(d.length, n.dynamicMainBullets + 4),
                  w =
                    (e.pagination.bulletSize * g - e.pagination.bulletSize) /
                      2 -
                    c * e.pagination.bulletSize,
                  b = t ? "right" : "left";
                d.css(e.isHorizontal() ? b : "top", w + "px");
              }
            }
            if (
              ("fraction" === n.type &&
                (a
                  .find(classesToSelector(n.currentClass))
                  .text(n.formatFractionCurrent(i + 1)),
                a
                  .find(classesToSelector(n.totalClass))
                  .text(n.formatFractionTotal(o))),
              "progressbar" === n.type)
            ) {
              var y;
              y = n.progressbarOpposite
                ? e.isHorizontal()
                  ? "vertical"
                  : "horizontal"
                : e.isHorizontal()
                ? "horizontal"
                : "vertical";
              var A = (i + 1) / o,
                x = 1,
                _ = 1;
              "horizontal" === y ? (x = A) : (_ = A),
                a
                  .find(classesToSelector(n.progressbarFillClass))
                  .transform(
                    "translate3d(0,0,0) scaleX(" + x + ") scaleY(" + _ + ")"
                  )
                  .transition(e.params.speed);
            }
            "custom" === n.type && n.renderCustom
              ? (a.html(n.renderCustom(e, i + 1, o)),
                e.emit("paginationRender", a[0]))
              : e.emit("paginationUpdate", a[0]),
              e.params.watchOverflow &&
                e.enabled &&
                a[e.isLocked ? "addClass" : "removeClass"](n.lockClass);
          }
        },
        render: function render() {
          var e = this,
            t = e.params.pagination;
          if (
            t.el &&
            e.pagination.el &&
            e.pagination.$el &&
            0 !== e.pagination.$el.length
          ) {
            var n =
                e.virtual && e.params.virtual.enabled
                  ? e.virtual.slides.length
                  : e.slides.length,
              i = e.pagination.$el,
              r = "";
            if ("bullets" === t.type) {
              var a = e.params.loop
                ? Math.ceil((n - 2 * e.loopedSlides) / e.params.slidesPerGroup)
                : e.snapGrid.length;
              e.params.freeMode && !e.params.loop && a > n && (a = n);
              for (var o = 0; o < a; o += 1)
                t.renderBullet
                  ? (r += t.renderBullet.call(e, o, t.bulletClass))
                  : (r +=
                      "<" +
                      t.bulletElement +
                      ' class="' +
                      t.bulletClass +
                      '"></' +
                      t.bulletElement +
                      ">");
              i.html(r),
                (e.pagination.bullets = i.find(
                  classesToSelector(t.bulletClass)
                ));
            }
            "fraction" === t.type &&
              ((r = t.renderFraction
                ? t.renderFraction.call(e, t.currentClass, t.totalClass)
                : '<span class="' +
                  t.currentClass +
                  '"></span> / <span class="' +
                  t.totalClass +
                  '"></span>'),
              i.html(r)),
              "progressbar" === t.type &&
                ((r = t.renderProgressbar
                  ? t.renderProgressbar.call(e, t.progressbarFillClass)
                  : '<span class="' + t.progressbarFillClass + '"></span>'),
                i.html(r)),
              "custom" !== t.type &&
                e.emit("paginationRender", e.pagination.$el[0]);
          }
        },
        init: function init() {
          var e = this;
          e.params.pagination = createElementIfNotDefined(
            e.$el,
            e.params.pagination,
            e.params.createElements,
            {
              el: "swiper-pagination",
            }
          );
          var t = e.params.pagination;
          if (t.el) {
            var n = $(t.el);
            0 !== n.length &&
              (e.params.uniqueNavElements &&
                "string" == typeof t.el &&
                n.length > 1 &&
                (n = e.$el.find(t.el)),
              "bullets" === t.type &&
                t.clickable &&
                n.addClass(t.clickableClass),
              n.addClass(t.modifierClass + t.type),
              "bullets" === t.type &&
                t.dynamicBullets &&
                (n.addClass("" + t.modifierClass + t.type + "-dynamic"),
                (e.pagination.dynamicBulletIndex = 0),
                t.dynamicMainBullets < 1 && (t.dynamicMainBullets = 1)),
              "progressbar" === t.type &&
                t.progressbarOpposite &&
                n.addClass(t.progressbarOppositeClass),
              t.clickable &&
                n.on(
                  "click",
                  classesToSelector(t.bulletClass),
                  function onClick(t) {
                    t.preventDefault();
                    var n = $(this).index() * e.params.slidesPerGroup;
                    e.params.loop && (n += e.loopedSlides), e.slideTo(n);
                  }
                ),
              extend(e.pagination, {
                $el: n,
                el: n[0],
              }),
              e.enabled || n.addClass(t.lockClass));
          }
        },
        destroy: function destroy() {
          var e = this,
            t = e.params.pagination;
          if (
            t.el &&
            e.pagination.el &&
            e.pagination.$el &&
            0 !== e.pagination.$el.length
          ) {
            var n = e.pagination.$el;
            n.removeClass(t.hiddenClass),
              n.removeClass(t.modifierClass + t.type),
              e.pagination.bullets &&
                e.pagination.bullets.removeClass(t.bulletActiveClass),
              t.clickable && n.off("click", classesToSelector(t.bulletClass));
          }
        },
      };
      function _extends$d() {
        return (
          (_extends$d =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$d.apply(this, arguments)
        );
      }
      var G = {
        setTranslate: function setTranslate() {
          var e = this;
          if (e.params.scrollbar.el && e.scrollbar.el) {
            var t = e.scrollbar,
              n = e.rtlTranslate,
              i = e.progress,
              r = t.dragSize,
              a = t.trackSize,
              o = t.$dragEl,
              s = t.$el,
              l = e.params.scrollbar,
              c = r,
              d = (a - r) * i;
            n
              ? (d = -d) > 0
                ? ((c = r - d), (d = 0))
                : -d + r > a && (c = a + d)
              : d < 0
              ? ((c = r + d), (d = 0))
              : d + r > a && (c = a - d),
              e.isHorizontal()
                ? (o.transform("translate3d(" + d + "px, 0, 0)"),
                  (o[0].style.width = c + "px"))
                : (o.transform("translate3d(0px, " + d + "px, 0)"),
                  (o[0].style.height = c + "px")),
              l.hide &&
                (clearTimeout(e.scrollbar.timeout),
                (s[0].style.opacity = 1),
                (e.scrollbar.timeout = setTimeout(function () {
                  (s[0].style.opacity = 0), s.transition(400);
                }, 1e3)));
          }
        },
        setTransition: function setTransition(e) {
          var t = this;
          t.params.scrollbar.el &&
            t.scrollbar.el &&
            t.scrollbar.$dragEl.transition(e);
        },
        updateSize: function updateSize() {
          var e = this;
          if (e.params.scrollbar.el && e.scrollbar.el) {
            var t = e.scrollbar,
              n = t.$dragEl,
              i = t.$el;
            (n[0].style.width = ""), (n[0].style.height = "");
            var r,
              a = e.isHorizontal() ? i[0].offsetWidth : i[0].offsetHeight,
              o = e.size / e.virtualSize,
              s = o * (a / e.size);
            (r =
              "auto" === e.params.scrollbar.dragSize
                ? a * o
                : parseInt(e.params.scrollbar.dragSize, 10)),
              e.isHorizontal()
                ? (n[0].style.width = r + "px")
                : (n[0].style.height = r + "px"),
              (i[0].style.display = o >= 1 ? "none" : ""),
              e.params.scrollbar.hide && (i[0].style.opacity = 0),
              extend(t, {
                trackSize: a,
                divider: o,
                moveDivider: s,
                dragSize: r,
              }),
              e.params.watchOverflow &&
                e.enabled &&
                t.$el[e.isLocked ? "addClass" : "removeClass"](
                  e.params.scrollbar.lockClass
                );
          }
        },
        getPointerPosition: function getPointerPosition(e) {
          return this.isHorizontal()
            ? "touchstart" === e.type || "touchmove" === e.type
              ? e.targetTouches[0].clientX
              : e.clientX
            : "touchstart" === e.type || "touchmove" === e.type
            ? e.targetTouches[0].clientY
            : e.clientY;
        },
        setDragPosition: function setDragPosition(e) {
          var t,
            n = this,
            i = n.scrollbar,
            r = n.rtlTranslate,
            a = i.$el,
            o = i.dragSize,
            s = i.trackSize,
            l = i.dragStartPos;
          (t =
            (i.getPointerPosition(e) -
              a.offset()[n.isHorizontal() ? "left" : "top"] -
              (null !== l ? l : o / 2)) /
            (s - o)),
            (t = Math.max(Math.min(t, 1), 0)),
            r && (t = 1 - t);
          var c = n.minTranslate() + (n.maxTranslate() - n.minTranslate()) * t;
          n.updateProgress(c),
            n.setTranslate(c),
            n.updateActiveIndex(),
            n.updateSlidesClasses();
        },
        onDragStart: function onDragStart(e) {
          var t = this,
            n = t.params.scrollbar,
            i = t.scrollbar,
            r = t.$wrapperEl,
            a = i.$el,
            o = i.$dragEl;
          (t.scrollbar.isTouched = !0),
            (t.scrollbar.dragStartPos =
              e.target === o[0] || e.target === o
                ? i.getPointerPosition(e) -
                  e.target.getBoundingClientRect()[
                    t.isHorizontal() ? "left" : "top"
                  ]
                : null),
            e.preventDefault(),
            e.stopPropagation(),
            r.transition(100),
            o.transition(100),
            i.setDragPosition(e),
            clearTimeout(t.scrollbar.dragTimeout),
            a.transition(0),
            n.hide && a.css("opacity", 1),
            t.params.cssMode && t.$wrapperEl.css("scroll-snap-type", "none"),
            t.emit("scrollbarDragStart", e);
        },
        onDragMove: function onDragMove(e) {
          var t = this,
            n = t.scrollbar,
            i = t.$wrapperEl,
            r = n.$el,
            a = n.$dragEl;
          t.scrollbar.isTouched &&
            (e.preventDefault ? e.preventDefault() : (e.returnValue = !1),
            n.setDragPosition(e),
            i.transition(0),
            r.transition(0),
            a.transition(0),
            t.emit("scrollbarDragMove", e));
        },
        onDragEnd: function onDragEnd(e) {
          var t = this,
            n = t.params.scrollbar,
            i = t.scrollbar,
            r = t.$wrapperEl,
            a = i.$el;
          t.scrollbar.isTouched &&
            ((t.scrollbar.isTouched = !1),
            t.params.cssMode &&
              (t.$wrapperEl.css("scroll-snap-type", ""), r.transition("")),
            n.hide &&
              (clearTimeout(t.scrollbar.dragTimeout),
              (t.scrollbar.dragTimeout = nextTick(function () {
                a.css("opacity", 0), a.transition(400);
              }, 1e3))),
            t.emit("scrollbarDragEnd", e),
            n.snapOnRelease && t.slideToClosest());
        },
        enableDraggable: function enableDraggable() {
          var e = this;
          if (e.params.scrollbar.el) {
            var t = getDocument(),
              n = e.scrollbar,
              i = e.touchEventsTouch,
              r = e.touchEventsDesktop,
              a = e.params,
              o = e.support,
              s = n.$el[0],
              l = !(!o.passiveListener || !a.passiveListeners) && {
                passive: !1,
                capture: !1,
              },
              c = !(!o.passiveListener || !a.passiveListeners) && {
                passive: !0,
                capture: !1,
              };
            s &&
              (o.touch
                ? (s.addEventListener(i.start, e.scrollbar.onDragStart, l),
                  s.addEventListener(i.move, e.scrollbar.onDragMove, l),
                  s.addEventListener(i.end, e.scrollbar.onDragEnd, c))
                : (s.addEventListener(r.start, e.scrollbar.onDragStart, l),
                  t.addEventListener(r.move, e.scrollbar.onDragMove, l),
                  t.addEventListener(r.end, e.scrollbar.onDragEnd, c)));
          }
        },
        disableDraggable: function disableDraggable() {
          var e = this;
          if (e.params.scrollbar.el) {
            var t = getDocument(),
              n = e.scrollbar,
              i = e.touchEventsTouch,
              r = e.touchEventsDesktop,
              a = e.params,
              o = e.support,
              s = n.$el[0],
              l = !(!o.passiveListener || !a.passiveListeners) && {
                passive: !1,
                capture: !1,
              },
              c = !(!o.passiveListener || !a.passiveListeners) && {
                passive: !0,
                capture: !1,
              };
            s &&
              (o.touch
                ? (s.removeEventListener(i.start, e.scrollbar.onDragStart, l),
                  s.removeEventListener(i.move, e.scrollbar.onDragMove, l),
                  s.removeEventListener(i.end, e.scrollbar.onDragEnd, c))
                : (s.removeEventListener(r.start, e.scrollbar.onDragStart, l),
                  t.removeEventListener(r.move, e.scrollbar.onDragMove, l),
                  t.removeEventListener(r.end, e.scrollbar.onDragEnd, c)));
          }
        },
        init: function init() {
          var e = this,
            t = e.scrollbar,
            n = e.$el;
          e.params.scrollbar = createElementIfNotDefined(
            n,
            e.params.scrollbar,
            e.params.createElements,
            {
              el: "swiper-scrollbar",
            }
          );
          var i = e.params.scrollbar;
          if (i.el) {
            var r = $(i.el);
            e.params.uniqueNavElements &&
              "string" == typeof i.el &&
              r.length > 1 &&
              1 === n.find(i.el).length &&
              (r = n.find(i.el));
            var a = r.find("." + e.params.scrollbar.dragClass);
            0 === a.length &&
              ((a = $(
                '<div class="' + e.params.scrollbar.dragClass + '"></div>'
              )),
              r.append(a)),
              extend(t, {
                $el: r,
                el: r[0],
                $dragEl: a,
                dragEl: a[0],
              }),
              i.draggable && t.enableDraggable(),
              r &&
                r[e.enabled ? "removeClass" : "addClass"](
                  e.params.scrollbar.lockClass
                );
          }
        },
        destroy: function destroy() {
          this.scrollbar.disableDraggable();
        },
      };
      function _extends$c() {
        return (
          (_extends$c =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$c.apply(this, arguments)
        );
      }
      var V = {
        setTransform: function setTransform(e, t) {
          var n = this.rtl,
            i = $(e),
            r = n ? -1 : 1,
            a = i.attr("data-swiper-parallax") || "0",
            o = i.attr("data-swiper-parallax-x"),
            s = i.attr("data-swiper-parallax-y"),
            l = i.attr("data-swiper-parallax-scale"),
            c = i.attr("data-swiper-parallax-opacity");
          if (
            (o || s
              ? ((o = o || "0"), (s = s || "0"))
              : this.isHorizontal()
              ? ((o = a), (s = "0"))
              : ((s = a), (o = "0")),
            (o =
              o.indexOf("%") >= 0
                ? parseInt(o, 10) * t * r + "%"
                : o * t * r + "px"),
            (s =
              s.indexOf("%") >= 0 ? parseInt(s, 10) * t + "%" : s * t + "px"),
            null != c)
          ) {
            var d = c - (c - 1) * (1 - Math.abs(t));
            i[0].style.opacity = d;
          }
          if (null == l) i.transform("translate3d(" + o + ", " + s + ", 0px)");
          else {
            var u = l - (l - 1) * (1 - Math.abs(t));
            i.transform(
              "translate3d(" + o + ", " + s + ", 0px) scale(" + u + ")"
            );
          }
        },
        setTranslate: function setTranslate() {
          var e = this,
            t = e.$el,
            n = e.slides,
            i = e.progress,
            r = e.snapGrid;
          t
            .children(
              "[data-swiper-parallax], [data-swiper-parallax-x], [data-swiper-parallax-y], [data-swiper-parallax-opacity], [data-swiper-parallax-scale]"
            )
            .each(function (t) {
              e.parallax.setTransform(t, i);
            }),
            n.each(function (t, n) {
              var a = t.progress;
              e.params.slidesPerGroup > 1 &&
                "auto" !== e.params.slidesPerView &&
                (a += Math.ceil(n / 2) - i * (r.length - 1)),
                (a = Math.min(Math.max(a, -1), 1)),
                $(t)
                  .find(
                    "[data-swiper-parallax], [data-swiper-parallax-x], [data-swiper-parallax-y], [data-swiper-parallax-opacity], [data-swiper-parallax-scale]"
                  )
                  .each(function (t) {
                    e.parallax.setTransform(t, a);
                  });
            });
        },
        setTransition: function setTransition(e) {
          void 0 === e && (e = this.params.speed);
          this.$el
            .find(
              "[data-swiper-parallax], [data-swiper-parallax-x], [data-swiper-parallax-y], [data-swiper-parallax-opacity], [data-swiper-parallax-scale]"
            )
            .each(function (t) {
              var n = $(t),
                i = parseInt(n.attr("data-swiper-parallax-duration"), 10) || e;
              0 === e && (i = 0), n.transition(i);
            });
        },
      };
      function _extends$b() {
        return (
          (_extends$b =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$b.apply(this, arguments)
        );
      }
      var W = {
        getDistanceBetweenTouches: function getDistanceBetweenTouches(e) {
          if (e.targetTouches.length < 2) return 1;
          var t = e.targetTouches[0].pageX,
            n = e.targetTouches[0].pageY,
            i = e.targetTouches[1].pageX,
            r = e.targetTouches[1].pageY;
          return Math.sqrt(Math.pow(i - t, 2) + Math.pow(r - n, 2));
        },
        onGestureStart: function onGestureStart(e) {
          var t = this,
            n = t.support,
            i = t.params.zoom,
            r = t.zoom,
            a = r.gesture;
          if (
            ((r.fakeGestureTouched = !1),
            (r.fakeGestureMoved = !1),
            !n.gestures)
          ) {
            if (
              "touchstart" !== e.type ||
              ("touchstart" === e.type && e.targetTouches.length < 2)
            )
              return;
            (r.fakeGestureTouched = !0),
              (a.scaleStart = W.getDistanceBetweenTouches(e));
          }
          (a.$slideEl && a.$slideEl.length) ||
          ((a.$slideEl = $(e.target).closest("." + t.params.slideClass)),
          0 === a.$slideEl.length && (a.$slideEl = t.slides.eq(t.activeIndex)),
          (a.$imageEl = a.$slideEl.find(
            "img, svg, canvas, picture, .swiper-zoom-target"
          )),
          (a.$imageWrapEl = a.$imageEl.parent("." + i.containerClass)),
          (a.maxRatio = a.$imageWrapEl.attr("data-swiper-zoom") || i.maxRatio),
          0 !== a.$imageWrapEl.length)
            ? (a.$imageEl && a.$imageEl.transition(0), (t.zoom.isScaling = !0))
            : (a.$imageEl = void 0);
        },
        onGestureChange: function onGestureChange(e) {
          var t = this,
            n = t.support,
            i = t.params.zoom,
            r = t.zoom,
            a = r.gesture;
          if (!n.gestures) {
            if (
              "touchmove" !== e.type ||
              ("touchmove" === e.type && e.targetTouches.length < 2)
            )
              return;
            (r.fakeGestureMoved = !0),
              (a.scaleMove = W.getDistanceBetweenTouches(e));
          }
          a.$imageEl && 0 !== a.$imageEl.length
            ? (n.gestures
                ? (r.scale = e.scale * r.currentScale)
                : (r.scale = (a.scaleMove / a.scaleStart) * r.currentScale),
              r.scale > a.maxRatio &&
                (r.scale =
                  a.maxRatio - 1 + Math.pow(r.scale - a.maxRatio + 1, 0.5)),
              r.scale < i.minRatio &&
                (r.scale =
                  i.minRatio + 1 - Math.pow(i.minRatio - r.scale + 1, 0.5)),
              a.$imageEl.transform("translate3d(0,0,0) scale(" + r.scale + ")"))
            : "gesturechange" === e.type && r.onGestureStart(e);
        },
        onGestureEnd: function onGestureEnd(e) {
          var t = this,
            n = t.device,
            i = t.support,
            r = t.params.zoom,
            a = t.zoom,
            o = a.gesture;
          if (!i.gestures) {
            if (!a.fakeGestureTouched || !a.fakeGestureMoved) return;
            if (
              "touchend" !== e.type ||
              ("touchend" === e.type &&
                e.changedTouches.length < 2 &&
                !n.android)
            )
              return;
            (a.fakeGestureTouched = !1), (a.fakeGestureMoved = !1);
          }
          o.$imageEl &&
            0 !== o.$imageEl.length &&
            ((a.scale = Math.max(Math.min(a.scale, o.maxRatio), r.minRatio)),
            o.$imageEl
              .transition(t.params.speed)
              .transform("translate3d(0,0,0) scale(" + a.scale + ")"),
            (a.currentScale = a.scale),
            (a.isScaling = !1),
            1 === a.scale && (o.$slideEl = void 0));
        },
        onTouchStart: function onTouchStart(e) {
          var t = this.device,
            n = this.zoom,
            i = n.gesture,
            r = n.image;
          i.$imageEl &&
            0 !== i.$imageEl.length &&
            (r.isTouched ||
              (t.android && e.cancelable && e.preventDefault(),
              (r.isTouched = !0),
              (r.touchesStart.x =
                "touchstart" === e.type ? e.targetTouches[0].pageX : e.pageX),
              (r.touchesStart.y =
                "touchstart" === e.type ? e.targetTouches[0].pageY : e.pageY)));
        },
        onTouchMove: function onTouchMove(e) {
          var t = this,
            n = t.zoom,
            i = n.gesture,
            r = n.image,
            a = n.velocity;
          if (
            i.$imageEl &&
            0 !== i.$imageEl.length &&
            ((t.allowClick = !1), r.isTouched && i.$slideEl)
          ) {
            r.isMoved ||
              ((r.width = i.$imageEl[0].offsetWidth),
              (r.height = i.$imageEl[0].offsetHeight),
              (r.startX = getTranslate(i.$imageWrapEl[0], "x") || 0),
              (r.startY = getTranslate(i.$imageWrapEl[0], "y") || 0),
              (i.slideWidth = i.$slideEl[0].offsetWidth),
              (i.slideHeight = i.$slideEl[0].offsetHeight),
              i.$imageWrapEl.transition(0));
            var o = r.width * n.scale,
              s = r.height * n.scale;
            if (!(o < i.slideWidth && s < i.slideHeight)) {
              if (
                ((r.minX = Math.min(i.slideWidth / 2 - o / 2, 0)),
                (r.maxX = -r.minX),
                (r.minY = Math.min(i.slideHeight / 2 - s / 2, 0)),
                (r.maxY = -r.minY),
                (r.touchesCurrent.x =
                  "touchmove" === e.type ? e.targetTouches[0].pageX : e.pageX),
                (r.touchesCurrent.y =
                  "touchmove" === e.type ? e.targetTouches[0].pageY : e.pageY),
                !r.isMoved && !n.isScaling)
              ) {
                if (
                  t.isHorizontal() &&
                  ((Math.floor(r.minX) === Math.floor(r.startX) &&
                    r.touchesCurrent.x < r.touchesStart.x) ||
                    (Math.floor(r.maxX) === Math.floor(r.startX) &&
                      r.touchesCurrent.x > r.touchesStart.x))
                )
                  return void (r.isTouched = !1);
                if (
                  !t.isHorizontal() &&
                  ((Math.floor(r.minY) === Math.floor(r.startY) &&
                    r.touchesCurrent.y < r.touchesStart.y) ||
                    (Math.floor(r.maxY) === Math.floor(r.startY) &&
                      r.touchesCurrent.y > r.touchesStart.y))
                )
                  return void (r.isTouched = !1);
              }
              e.cancelable && e.preventDefault(),
                e.stopPropagation(),
                (r.isMoved = !0),
                (r.currentX = r.touchesCurrent.x - r.touchesStart.x + r.startX),
                (r.currentY = r.touchesCurrent.y - r.touchesStart.y + r.startY),
                r.currentX < r.minX &&
                  (r.currentX =
                    r.minX + 1 - Math.pow(r.minX - r.currentX + 1, 0.8)),
                r.currentX > r.maxX &&
                  (r.currentX =
                    r.maxX - 1 + Math.pow(r.currentX - r.maxX + 1, 0.8)),
                r.currentY < r.minY &&
                  (r.currentY =
                    r.minY + 1 - Math.pow(r.minY - r.currentY + 1, 0.8)),
                r.currentY > r.maxY &&
                  (r.currentY =
                    r.maxY - 1 + Math.pow(r.currentY - r.maxY + 1, 0.8)),
                a.prevPositionX || (a.prevPositionX = r.touchesCurrent.x),
                a.prevPositionY || (a.prevPositionY = r.touchesCurrent.y),
                a.prevTime || (a.prevTime = Date.now()),
                (a.x =
                  (r.touchesCurrent.x - a.prevPositionX) /
                  (Date.now() - a.prevTime) /
                  2),
                (a.y =
                  (r.touchesCurrent.y - a.prevPositionY) /
                  (Date.now() - a.prevTime) /
                  2),
                Math.abs(r.touchesCurrent.x - a.prevPositionX) < 2 && (a.x = 0),
                Math.abs(r.touchesCurrent.y - a.prevPositionY) < 2 && (a.y = 0),
                (a.prevPositionX = r.touchesCurrent.x),
                (a.prevPositionY = r.touchesCurrent.y),
                (a.prevTime = Date.now()),
                i.$imageWrapEl.transform(
                  "translate3d(" + r.currentX + "px, " + r.currentY + "px,0)"
                );
            }
          }
        },
        onTouchEnd: function onTouchEnd() {
          var e = this.zoom,
            t = e.gesture,
            n = e.image,
            i = e.velocity;
          if (t.$imageEl && 0 !== t.$imageEl.length) {
            if (!n.isTouched || !n.isMoved)
              return (n.isTouched = !1), void (n.isMoved = !1);
            (n.isTouched = !1), (n.isMoved = !1);
            var r = 300,
              a = 300,
              o = i.x * r,
              s = n.currentX + o,
              l = i.y * a,
              c = n.currentY + l;
            0 !== i.x && (r = Math.abs((s - n.currentX) / i.x)),
              0 !== i.y && (a = Math.abs((c - n.currentY) / i.y));
            var d = Math.max(r, a);
            (n.currentX = s), (n.currentY = c);
            var u = n.width * e.scale,
              p = n.height * e.scale;
            (n.minX = Math.min(t.slideWidth / 2 - u / 2, 0)),
              (n.maxX = -n.minX),
              (n.minY = Math.min(t.slideHeight / 2 - p / 2, 0)),
              (n.maxY = -n.minY),
              (n.currentX = Math.max(Math.min(n.currentX, n.maxX), n.minX)),
              (n.currentY = Math.max(Math.min(n.currentY, n.maxY), n.minY)),
              t.$imageWrapEl
                .transition(d)
                .transform(
                  "translate3d(" + n.currentX + "px, " + n.currentY + "px,0)"
                );
          }
        },
        onTransitionEnd: function onTransitionEnd() {
          var e = this,
            t = e.zoom,
            n = t.gesture;
          n.$slideEl &&
            e.previousIndex !== e.activeIndex &&
            (n.$imageEl && n.$imageEl.transform("translate3d(0,0,0) scale(1)"),
            n.$imageWrapEl && n.$imageWrapEl.transform("translate3d(0,0,0)"),
            (t.scale = 1),
            (t.currentScale = 1),
            (n.$slideEl = void 0),
            (n.$imageEl = void 0),
            (n.$imageWrapEl = void 0));
        },
        toggle: function toggle(e) {
          var t = this.zoom;
          t.scale && 1 !== t.scale ? t.out() : t.in(e);
        },
        in: function _in(e) {
          var t,
            n,
            i,
            r,
            a,
            o,
            s,
            l,
            c,
            d,
            u,
            p,
            h,
            m,
            f,
            v,
            g = this,
            w = getWindow(),
            b = g.zoom,
            y = g.params.zoom,
            A = b.gesture,
            x = b.image;
          (A.$slideEl ||
            (e &&
              e.target &&
              (A.$slideEl = $(e.target).closest("." + g.params.slideClass)),
            A.$slideEl ||
              (g.params.virtual && g.params.virtual.enabled && g.virtual
                ? (A.$slideEl = g.$wrapperEl.children(
                    "." + g.params.slideActiveClass
                  ))
                : (A.$slideEl = g.slides.eq(g.activeIndex))),
            (A.$imageEl = A.$slideEl.find(
              "img, svg, canvas, picture, .swiper-zoom-target"
            )),
            (A.$imageWrapEl = A.$imageEl.parent("." + y.containerClass))),
          A.$imageEl &&
            0 !== A.$imageEl.length &&
            A.$imageWrapEl &&
            0 !== A.$imageWrapEl.length) &&
            (A.$slideEl.addClass("" + y.zoomedSlideClass),
            void 0 === x.touchesStart.x && e
              ? ((t =
                  "touchend" === e.type ? e.changedTouches[0].pageX : e.pageX),
                (n =
                  "touchend" === e.type ? e.changedTouches[0].pageY : e.pageY))
              : ((t = x.touchesStart.x), (n = x.touchesStart.y)),
            (b.scale = A.$imageWrapEl.attr("data-swiper-zoom") || y.maxRatio),
            (b.currentScale =
              A.$imageWrapEl.attr("data-swiper-zoom") || y.maxRatio),
            e
              ? ((f = A.$slideEl[0].offsetWidth),
                (v = A.$slideEl[0].offsetHeight),
                (i = A.$slideEl.offset().left + w.scrollX + f / 2 - t),
                (r = A.$slideEl.offset().top + w.scrollY + v / 2 - n),
                (s = A.$imageEl[0].offsetWidth),
                (l = A.$imageEl[0].offsetHeight),
                (c = s * b.scale),
                (d = l * b.scale),
                (h = -(u = Math.min(f / 2 - c / 2, 0))),
                (m = -(p = Math.min(v / 2 - d / 2, 0))),
                (a = i * b.scale) < u && (a = u),
                a > h && (a = h),
                (o = r * b.scale) < p && (o = p),
                o > m && (o = m))
              : ((a = 0), (o = 0)),
            A.$imageWrapEl
              .transition(300)
              .transform("translate3d(" + a + "px, " + o + "px,0)"),
            A.$imageEl
              .transition(300)
              .transform("translate3d(0,0,0) scale(" + b.scale + ")"));
        },
        out: function out() {
          var e = this,
            t = e.zoom,
            n = e.params.zoom,
            i = t.gesture;
          i.$slideEl ||
            (e.params.virtual && e.params.virtual.enabled && e.virtual
              ? (i.$slideEl = e.$wrapperEl.children(
                  "." + e.params.slideActiveClass
                ))
              : (i.$slideEl = e.slides.eq(e.activeIndex)),
            (i.$imageEl = i.$slideEl.find(
              "img, svg, canvas, picture, .swiper-zoom-target"
            )),
            (i.$imageWrapEl = i.$imageEl.parent("." + n.containerClass))),
            i.$imageEl &&
              0 !== i.$imageEl.length &&
              i.$imageWrapEl &&
              0 !== i.$imageWrapEl.length &&
              ((t.scale = 1),
              (t.currentScale = 1),
              i.$imageWrapEl.transition(300).transform("translate3d(0,0,0)"),
              i.$imageEl
                .transition(300)
                .transform("translate3d(0,0,0) scale(1)"),
              i.$slideEl.removeClass("" + n.zoomedSlideClass),
              (i.$slideEl = void 0));
        },
        toggleGestures: function toggleGestures(e) {
          var t = this,
            n = t.zoom,
            i = n.slideSelector,
            r = n.passiveListener;
          t.$wrapperEl[e]("gesturestart", i, n.onGestureStart, r),
            t.$wrapperEl[e]("gesturechange", i, n.onGestureChange, r),
            t.$wrapperEl[e]("gestureend", i, n.onGestureEnd, r);
        },
        enableGestures: function enableGestures() {
          this.zoom.gesturesEnabled ||
            ((this.zoom.gesturesEnabled = !0), this.zoom.toggleGestures("on"));
        },
        disableGestures: function disableGestures() {
          this.zoom.gesturesEnabled &&
            ((this.zoom.gesturesEnabled = !1), this.zoom.toggleGestures("off"));
        },
        enable: function enable() {
          var e = this,
            t = e.support,
            n = e.zoom;
          if (!n.enabled) {
            n.enabled = !0;
            var i = !(
                "touchstart" !== e.touchEvents.start ||
                !t.passiveListener ||
                !e.params.passiveListeners
              ) && {
                passive: !0,
                capture: !1,
              },
              r = !t.passiveListener || {
                passive: !1,
                capture: !0,
              },
              a = "." + e.params.slideClass;
            (e.zoom.passiveListener = i),
              (e.zoom.slideSelector = a),
              t.gestures
                ? (e.$wrapperEl.on(
                    e.touchEvents.start,
                    e.zoom.enableGestures,
                    i
                  ),
                  e.$wrapperEl.on(e.touchEvents.end, e.zoom.disableGestures, i))
                : "touchstart" === e.touchEvents.start &&
                  (e.$wrapperEl.on(e.touchEvents.start, a, n.onGestureStart, i),
                  e.$wrapperEl.on(e.touchEvents.move, a, n.onGestureChange, r),
                  e.$wrapperEl.on(e.touchEvents.end, a, n.onGestureEnd, i),
                  e.touchEvents.cancel &&
                    e.$wrapperEl.on(
                      e.touchEvents.cancel,
                      a,
                      n.onGestureEnd,
                      i
                    )),
              e.$wrapperEl.on(
                e.touchEvents.move,
                "." + e.params.zoom.containerClass,
                n.onTouchMove,
                r
              );
          }
        },
        disable: function disable() {
          var e = this,
            t = e.zoom;
          if (t.enabled) {
            var n = e.support;
            e.zoom.enabled = !1;
            var i = !(
                "touchstart" !== e.touchEvents.start ||
                !n.passiveListener ||
                !e.params.passiveListeners
              ) && {
                passive: !0,
                capture: !1,
              },
              r = !n.passiveListener || {
                passive: !1,
                capture: !0,
              },
              a = "." + e.params.slideClass;
            n.gestures
              ? (e.$wrapperEl.off(
                  e.touchEvents.start,
                  e.zoom.enableGestures,
                  i
                ),
                e.$wrapperEl.off(e.touchEvents.end, e.zoom.disableGestures, i))
              : "touchstart" === e.touchEvents.start &&
                (e.$wrapperEl.off(e.touchEvents.start, a, t.onGestureStart, i),
                e.$wrapperEl.off(e.touchEvents.move, a, t.onGestureChange, r),
                e.$wrapperEl.off(e.touchEvents.end, a, t.onGestureEnd, i),
                e.touchEvents.cancel &&
                  e.$wrapperEl.off(e.touchEvents.cancel, a, t.onGestureEnd, i)),
              e.$wrapperEl.off(
                e.touchEvents.move,
                "." + e.params.zoom.containerClass,
                t.onTouchMove,
                r
              );
          }
        },
      };
      function _extends$a() {
        return (
          (_extends$a =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$a.apply(this, arguments)
        );
      }
      var H = {
        loadInSlide: function loadInSlide(e, t) {
          void 0 === t && (t = !0);
          var n = this,
            i = n.params.lazy;
          if (void 0 !== e && 0 !== n.slides.length) {
            var r =
                n.virtual && n.params.virtual.enabled
                  ? n.$wrapperEl.children(
                      "." +
                        n.params.slideClass +
                        '[data-swiper-slide-index="' +
                        e +
                        '"]'
                    )
                  : n.slides.eq(e),
              a = r.find(
                "." +
                  i.elementClass +
                  ":not(." +
                  i.loadedClass +
                  "):not(." +
                  i.loadingClass +
                  ")"
              );
            !r.hasClass(i.elementClass) ||
              r.hasClass(i.loadedClass) ||
              r.hasClass(i.loadingClass) ||
              a.push(r[0]),
              0 !== a.length &&
                a.each(function (e) {
                  var a = $(e);
                  a.addClass(i.loadingClass);
                  var o = a.attr("data-background"),
                    s = a.attr("data-src"),
                    l = a.attr("data-srcset"),
                    c = a.attr("data-sizes"),
                    d = a.parent("picture");
                  n.loadImage(a[0], s || o, l, c, !1, function () {
                    if (null != n && n && (!n || n.params) && !n.destroyed) {
                      if (
                        (o
                          ? (a.css("background-image", 'url("' + o + '")'),
                            a.removeAttr("data-background"))
                          : (l &&
                              (a.attr("srcset", l),
                              a.removeAttr("data-srcset")),
                            c &&
                              (a.attr("sizes", c), a.removeAttr("data-sizes")),
                            d.length &&
                              d.children("source").each(function (e) {
                                var t = $(e);
                                t.attr("data-srcset") &&
                                  (t.attr("srcset", t.attr("data-srcset")),
                                  t.removeAttr("data-srcset"));
                              }),
                            s && (a.attr("src", s), a.removeAttr("data-src"))),
                        a.addClass(i.loadedClass).removeClass(i.loadingClass),
                        r.find("." + i.preloaderClass).remove(),
                        n.params.loop && t)
                      ) {
                        var e = r.attr("data-swiper-slide-index");
                        if (r.hasClass(n.params.slideDuplicateClass)) {
                          var u = n.$wrapperEl.children(
                            '[data-swiper-slide-index="' +
                              e +
                              '"]:not(.' +
                              n.params.slideDuplicateClass +
                              ")"
                          );
                          n.lazy.loadInSlide(u.index(), !1);
                        } else {
                          var p = n.$wrapperEl.children(
                            "." +
                              n.params.slideDuplicateClass +
                              '[data-swiper-slide-index="' +
                              e +
                              '"]'
                          );
                          n.lazy.loadInSlide(p.index(), !1);
                        }
                      }
                      n.emit("lazyImageReady", r[0], a[0]),
                        n.params.autoHeight && n.updateAutoHeight();
                    }
                  }),
                    n.emit("lazyImageLoad", r[0], a[0]);
                });
          }
        },
        load: function load() {
          var e = this,
            t = e.$wrapperEl,
            n = e.params,
            i = e.slides,
            r = e.activeIndex,
            a = e.virtual && n.virtual.enabled,
            o = n.lazy,
            s = n.slidesPerView;
          function slideExist(e) {
            if (a) {
              if (
                t.children(
                  "." + n.slideClass + '[data-swiper-slide-index="' + e + '"]'
                ).length
              )
                return !0;
            } else if (i[e]) return !0;
            return !1;
          }
          function slideIndex(e) {
            return a ? $(e).attr("data-swiper-slide-index") : $(e).index();
          }
          if (
            ("auto" === s && (s = 0),
            e.lazy.initialImageLoaded || (e.lazy.initialImageLoaded = !0),
            e.params.watchSlidesVisibility)
          )
            t.children("." + n.slideVisibleClass).each(function (t) {
              var n = a ? $(t).attr("data-swiper-slide-index") : $(t).index();
              e.lazy.loadInSlide(n);
            });
          else if (s > 1)
            for (var l = r; l < r + s; l += 1)
              slideExist(l) && e.lazy.loadInSlide(l);
          else e.lazy.loadInSlide(r);
          if (o.loadPrevNext)
            if (s > 1 || (o.loadPrevNextAmount && o.loadPrevNextAmount > 1)) {
              for (
                var c = o.loadPrevNextAmount,
                  d = s,
                  u = Math.min(r + d + Math.max(c, d), i.length),
                  p = Math.max(r - Math.max(d, c), 0),
                  h = r + s;
                h < u;
                h += 1
              )
                slideExist(h) && e.lazy.loadInSlide(h);
              for (var m = p; m < r; m += 1)
                slideExist(m) && e.lazy.loadInSlide(m);
            } else {
              var f = t.children("." + n.slideNextClass);
              f.length > 0 && e.lazy.loadInSlide(slideIndex(f));
              var v = t.children("." + n.slidePrevClass);
              v.length > 0 && e.lazy.loadInSlide(slideIndex(v));
            }
        },
        checkInViewOnLoad: function checkInViewOnLoad() {
          var e = getWindow(),
            t = this;
          if (t && !t.destroyed) {
            var n = t.params.lazy.scrollingElement
                ? $(t.params.lazy.scrollingElement)
                : $(e),
              i = n[0] === e,
              r = i ? e.innerWidth : n[0].offsetWidth,
              a = i ? e.innerHeight : n[0].offsetHeight,
              o = t.$el.offset(),
              s = !1;
            t.rtlTranslate && (o.left -= t.$el[0].scrollLeft);
            for (
              var l = [
                  [o.left, o.top],
                  [o.left + t.width, o.top],
                  [o.left, o.top + t.height],
                  [o.left + t.width, o.top + t.height],
                ],
                c = 0;
              c < l.length;
              c += 1
            ) {
              var d = l[c];
              if (d[0] >= 0 && d[0] <= r && d[1] >= 0 && d[1] <= a) {
                if (0 === d[0] && 0 === d[1]) continue;
                s = !0;
              }
            }
            var u = !(
              "touchstart" !== t.touchEvents.start ||
              !t.support.passiveListener ||
              !t.params.passiveListeners
            ) && {
              passive: !0,
              capture: !1,
            };
            s
              ? (t.lazy.load(), n.off("scroll", t.lazy.checkInViewOnLoad, u))
              : t.lazy.scrollHandlerAttached ||
                ((t.lazy.scrollHandlerAttached = !0),
                n.on("scroll", t.lazy.checkInViewOnLoad, u));
          }
        },
      };
      function _extends$9() {
        return (
          (_extends$9 =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$9.apply(this, arguments)
        );
      }
      var X = {
          LinearSpline: function LinearSpline(e, t) {
            var n,
              i,
              r = (function search() {
                var e, t, n;
                return function (i, r) {
                  for (t = -1, e = i.length; e - t > 1; )
                    i[(n = (e + t) >> 1)] <= r ? (t = n) : (e = n);
                  return e;
                };
              })();
            return (
              (this.x = e),
              (this.y = t),
              (this.lastIndex = e.length - 1),
              (this.interpolate = function interpolate(e) {
                return e
                  ? ((i = r(this.x, e)),
                    (n = i - 1),
                    ((e - this.x[n]) * (this.y[i] - this.y[n])) /
                      (this.x[i] - this.x[n]) +
                      this.y[n])
                  : 0;
              }),
              this
            );
          },
          getInterpolateFunction: function getInterpolateFunction(e) {
            var t = this;
            t.controller.spline ||
              (t.controller.spline = t.params.loop
                ? new X.LinearSpline(t.slidesGrid, e.slidesGrid)
                : new X.LinearSpline(t.snapGrid, e.snapGrid));
          },
          setTranslate: function setTranslate(e, t) {
            var n,
              i,
              r = this,
              a = r.controller.control,
              o = r.constructor;
            function setControlledTranslate(e) {
              var t = r.rtlTranslate ? -r.translate : r.translate;
              "slide" === r.params.controller.by &&
                (r.controller.getInterpolateFunction(e),
                (i = -r.controller.spline.interpolate(-t))),
                (i && "container" !== r.params.controller.by) ||
                  ((n =
                    (e.maxTranslate() - e.minTranslate()) /
                    (r.maxTranslate() - r.minTranslate())),
                  (i = (t - r.minTranslate()) * n + e.minTranslate())),
                r.params.controller.inverse && (i = e.maxTranslate() - i),
                e.updateProgress(i),
                e.setTranslate(i, r),
                e.updateActiveIndex(),
                e.updateSlidesClasses();
            }
            if (Array.isArray(a))
              for (var s = 0; s < a.length; s += 1)
                a[s] !== t && a[s] instanceof o && setControlledTranslate(a[s]);
            else a instanceof o && t !== a && setControlledTranslate(a);
          },
          setTransition: function setTransition(e, t) {
            var n,
              i = this,
              r = i.constructor,
              a = i.controller.control;
            function setControlledTransition(t) {
              t.setTransition(e, i),
                0 !== e &&
                  (t.transitionStart(),
                  t.params.autoHeight &&
                    nextTick(function () {
                      t.updateAutoHeight();
                    }),
                  t.$wrapperEl.transitionEnd(function () {
                    a &&
                      (t.params.loop &&
                        "slide" === i.params.controller.by &&
                        t.loopFix(),
                      t.transitionEnd());
                  }));
            }
            if (Array.isArray(a))
              for (n = 0; n < a.length; n += 1)
                a[n] !== t &&
                  a[n] instanceof r &&
                  setControlledTransition(a[n]);
            else a instanceof r && t !== a && setControlledTransition(a);
          },
        },
        U = {
          name: "controller",
          params: {
            controller: {
              control: void 0,
              inverse: !1,
              by: "slide",
            },
          },
          create: function create() {
            bindModuleMethods(this, {
              controller: _extends$9(
                {
                  control: this.params.controller.control,
                },
                X
              ),
            });
          },
          on: {
            update: function update(e) {
              e.controller.control &&
                e.controller.spline &&
                ((e.controller.spline = void 0), delete e.controller.spline);
            },
            resize: function resize(e) {
              e.controller.control &&
                e.controller.spline &&
                ((e.controller.spline = void 0), delete e.controller.spline);
            },
            observerUpdate: function observerUpdate(e) {
              e.controller.control &&
                e.controller.spline &&
                ((e.controller.spline = void 0), delete e.controller.spline);
            },
            setTranslate: function setTranslate(e, t, n) {
              e.controller.control && e.controller.setTranslate(t, n);
            },
            setTransition: function setTransition(e, t, n) {
              e.controller.control && e.controller.setTransition(t, n);
            },
          },
        };
      function _extends$8() {
        return (
          (_extends$8 =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$8.apply(this, arguments)
        );
      }
      var Z = {
        getRandomNumber: function getRandomNumber(e) {
          void 0 === e && (e = 16);
          return "x".repeat(e).replace(/x/g, function randomChar() {
            return Math.round(16 * Math.random()).toString(16);
          });
        },
        makeElFocusable: function makeElFocusable(e) {
          return e.attr("tabIndex", "0"), e;
        },
        makeElNotFocusable: function makeElNotFocusable(e) {
          return e.attr("tabIndex", "-1"), e;
        },
        addElRole: function addElRole(e, t) {
          return e.attr("role", t), e;
        },
        addElRoleDescription: function addElRoleDescription(e, t) {
          return e.attr("aria-roledescription", t), e;
        },
        addElControls: function addElControls(e, t) {
          return e.attr("aria-controls", t), e;
        },
        addElLabel: function addElLabel(e, t) {
          return e.attr("aria-label", t), e;
        },
        addElId: function addElId(e, t) {
          return e.attr("id", t), e;
        },
        addElLive: function addElLive(e, t) {
          return e.attr("aria-live", t), e;
        },
        disableEl: function disableEl(e) {
          return e.attr("aria-disabled", !0), e;
        },
        enableEl: function enableEl(e) {
          return e.attr("aria-disabled", !1), e;
        },
        onEnterOrSpaceKey: function onEnterOrSpaceKey(e) {
          if (13 === e.keyCode || 32 === e.keyCode) {
            var t = this,
              n = t.params.a11y,
              i = $(e.target);
            t.navigation &&
              t.navigation.$nextEl &&
              i.is(t.navigation.$nextEl) &&
              ((t.isEnd && !t.params.loop) || t.slideNext(),
              t.isEnd
                ? t.a11y.notify(n.lastSlideMessage)
                : t.a11y.notify(n.nextSlideMessage)),
              t.navigation &&
                t.navigation.$prevEl &&
                i.is(t.navigation.$prevEl) &&
                ((t.isBeginning && !t.params.loop) || t.slidePrev(),
                t.isBeginning
                  ? t.a11y.notify(n.firstSlideMessage)
                  : t.a11y.notify(n.prevSlideMessage)),
              t.pagination &&
                i.is(classesToSelector(t.params.pagination.bulletClass)) &&
                i[0].click();
          }
        },
        notify: function notify(e) {
          var t = this.a11y.liveRegion;
          0 !== t.length && (t.html(""), t.html(e));
        },
        updateNavigation: function updateNavigation() {
          var e = this;
          if (!e.params.loop && e.navigation) {
            var t = e.navigation,
              n = t.$nextEl,
              i = t.$prevEl;
            i &&
              i.length > 0 &&
              (e.isBeginning
                ? (e.a11y.disableEl(i), e.a11y.makeElNotFocusable(i))
                : (e.a11y.enableEl(i), e.a11y.makeElFocusable(i))),
              n &&
                n.length > 0 &&
                (e.isEnd
                  ? (e.a11y.disableEl(n), e.a11y.makeElNotFocusable(n))
                  : (e.a11y.enableEl(n), e.a11y.makeElFocusable(n)));
          }
        },
        updatePagination: function updatePagination() {
          var e = this,
            t = e.params.a11y;
          e.pagination &&
            e.params.pagination.clickable &&
            e.pagination.bullets &&
            e.pagination.bullets.length &&
            e.pagination.bullets.each(function (n) {
              var i = $(n);
              e.a11y.makeElFocusable(i),
                e.params.pagination.renderBullet ||
                  (e.a11y.addElRole(i, "button"),
                  e.a11y.addElLabel(
                    i,
                    t.paginationBulletMessage.replace(
                      /\{\{index\}\}/,
                      i.index() + 1
                    )
                  ));
            });
        },
        init: function init() {
          var e = this,
            t = e.params.a11y;
          e.$el.append(e.a11y.liveRegion);
          var n = e.$el;
          t.containerRoleDescriptionMessage &&
            e.a11y.addElRoleDescription(n, t.containerRoleDescriptionMessage),
            t.containerMessage && e.a11y.addElLabel(n, t.containerMessage);
          var i = e.$wrapperEl,
            r = i.attr("id") || "swiper-wrapper-" + e.a11y.getRandomNumber(16),
            a =
              e.params.autoplay && e.params.autoplay.enabled ? "off" : "polite";
          e.a11y.addElId(i, r),
            e.a11y.addElLive(i, a),
            t.itemRoleDescriptionMessage &&
              e.a11y.addElRoleDescription(
                $(e.slides),
                t.itemRoleDescriptionMessage
              ),
            e.a11y.addElRole($(e.slides), t.slideRole);
          var o,
            s,
            l = e.params.loop
              ? e.slides.filter(function (t) {
                  return !t.classList.contains(e.params.slideDuplicateClass);
                }).length
              : e.slides.length;
          e.slides.each(function (n, i) {
            var r = $(n),
              a = e.params.loop
                ? parseInt(r.attr("data-swiper-slide-index"), 10)
                : i,
              o = t.slideLabelMessage
                .replace(/\{\{index\}\}/, a + 1)
                .replace(/\{\{slidesLength\}\}/, l);
            e.a11y.addElLabel(r, o);
          }),
            e.navigation && e.navigation.$nextEl && (o = e.navigation.$nextEl),
            e.navigation && e.navigation.$prevEl && (s = e.navigation.$prevEl),
            o &&
              o.length &&
              (e.a11y.makeElFocusable(o),
              "BUTTON" !== o[0].tagName &&
                (e.a11y.addElRole(o, "button"),
                o.on("keydown", e.a11y.onEnterOrSpaceKey)),
              e.a11y.addElLabel(o, t.nextSlideMessage),
              e.a11y.addElControls(o, r)),
            s &&
              s.length &&
              (e.a11y.makeElFocusable(s),
              "BUTTON" !== s[0].tagName &&
                (e.a11y.addElRole(s, "button"),
                s.on("keydown", e.a11y.onEnterOrSpaceKey)),
              e.a11y.addElLabel(s, t.prevSlideMessage),
              e.a11y.addElControls(s, r)),
            e.pagination &&
              e.params.pagination.clickable &&
              e.pagination.bullets &&
              e.pagination.bullets.length &&
              e.pagination.$el.on(
                "keydown",
                classesToSelector(e.params.pagination.bulletClass),
                e.a11y.onEnterOrSpaceKey
              );
        },
        destroy: function destroy() {
          var e,
            t,
            n = this;
          n.a11y.liveRegion &&
            n.a11y.liveRegion.length > 0 &&
            n.a11y.liveRegion.remove(),
            n.navigation && n.navigation.$nextEl && (e = n.navigation.$nextEl),
            n.navigation && n.navigation.$prevEl && (t = n.navigation.$prevEl),
            e && e.off("keydown", n.a11y.onEnterOrSpaceKey),
            t && t.off("keydown", n.a11y.onEnterOrSpaceKey),
            n.pagination &&
              n.params.pagination.clickable &&
              n.pagination.bullets &&
              n.pagination.bullets.length &&
              n.pagination.$el.off(
                "keydown",
                classesToSelector(n.params.pagination.bulletClass),
                n.a11y.onEnterOrSpaceKey
              );
        },
      };
      function _extends$7() {
        return (
          (_extends$7 =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$7.apply(this, arguments)
        );
      }
      var Q = {
        init: function init() {
          var e = this,
            t = getWindow();
          if (e.params.history) {
            if (!t.history || !t.history.pushState)
              return (
                (e.params.history.enabled = !1),
                void (e.params.hashNavigation.enabled = !0)
              );
            var n = e.history;
            (n.initialized = !0),
              (n.paths = Q.getPathValues(e.params.url)),
              (n.paths.key || n.paths.value) &&
                (n.scrollToSlide(0, n.paths.value, e.params.runCallbacksOnInit),
                e.params.history.replaceState ||
                  t.addEventListener("popstate", e.history.setHistoryPopState));
          }
        },
        destroy: function destroy() {
          var e = getWindow();
          this.params.history.replaceState ||
            e.removeEventListener("popstate", this.history.setHistoryPopState);
        },
        setHistoryPopState: function setHistoryPopState() {
          var e = this;
          (e.history.paths = Q.getPathValues(e.params.url)),
            e.history.scrollToSlide(e.params.speed, e.history.paths.value, !1);
        },
        getPathValues: function getPathValues(e) {
          var t = getWindow(),
            n = (e ? new URL(e) : t.location).pathname
              .slice(1)
              .split("/")
              .filter(function (e) {
                return "" !== e;
              }),
            i = n.length;
          return {
            key: n[i - 2],
            value: n[i - 1],
          };
        },
        setHistory: function setHistory(e, t) {
          var n = this,
            i = getWindow();
          if (n.history.initialized && n.params.history.enabled) {
            var r;
            r = n.params.url ? new URL(n.params.url) : i.location;
            var a = n.slides.eq(t),
              o = Q.slugify(a.attr("data-history"));
            if (n.params.history.root.length > 0) {
              var s = n.params.history.root;
              "/" === s[s.length - 1] && (s = s.slice(0, s.length - 1)),
                (o = s + "/" + e + "/" + o);
            } else r.pathname.includes(e) || (o = e + "/" + o);
            var l = i.history.state;
            (l && l.value === o) ||
              (n.params.history.replaceState
                ? i.history.replaceState(
                    {
                      value: o,
                    },
                    null,
                    o
                  )
                : i.history.pushState(
                    {
                      value: o,
                    },
                    null,
                    o
                  ));
          }
        },
        slugify: function slugify(e) {
          return e
            .toString()
            .replace(/\s+/g, "-")
            .replace(/[^\w-]+/g, "")
            .replace(/--+/g, "-")
            .replace(/^-+/, "")
            .replace(/-+$/, "");
        },
        scrollToSlide: function scrollToSlide(e, t, n) {
          var i = this;
          if (t)
            for (var r = 0, a = i.slides.length; r < a; r += 1) {
              var o = i.slides.eq(r);
              if (
                Q.slugify(o.attr("data-history")) === t &&
                !o.hasClass(i.params.slideDuplicateClass)
              ) {
                var s = o.index();
                i.slideTo(s, e, n);
              }
            }
          else i.slideTo(0, e, n);
        },
      };
      function _extends$6() {
        return (
          (_extends$6 =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$6.apply(this, arguments)
        );
      }
      var q = {
        onHashChange: function onHashChange() {
          var e = this,
            t = getDocument();
          e.emit("hashChange");
          var n = t.location.hash.replace("#", "");
          if (n !== e.slides.eq(e.activeIndex).attr("data-hash")) {
            var i = e.$wrapperEl
              .children("." + e.params.slideClass + '[data-hash="' + n + '"]')
              .index();
            if (void 0 === i) return;
            e.slideTo(i);
          }
        },
        setHash: function setHash() {
          var e = this,
            t = getWindow(),
            n = getDocument();
          if (e.hashNavigation.initialized && e.params.hashNavigation.enabled)
            if (
              e.params.hashNavigation.replaceState &&
              t.history &&
              t.history.replaceState
            )
              t.history.replaceState(
                null,
                null,
                "#" + e.slides.eq(e.activeIndex).attr("data-hash") || 0
              ),
                e.emit("hashSet");
            else {
              var i = e.slides.eq(e.activeIndex),
                r = i.attr("data-hash") || i.attr("data-history");
              (n.location.hash = r || ""), e.emit("hashSet");
            }
        },
        init: function init() {
          var e = this,
            t = getDocument(),
            n = getWindow();
          if (
            !(
              !e.params.hashNavigation.enabled ||
              (e.params.history && e.params.history.enabled)
            )
          ) {
            e.hashNavigation.initialized = !0;
            var i = t.location.hash.replace("#", "");
            if (i)
              for (var r = 0, a = e.slides.length; r < a; r += 1) {
                var o = e.slides.eq(r);
                if (
                  (o.attr("data-hash") || o.attr("data-history")) === i &&
                  !o.hasClass(e.params.slideDuplicateClass)
                ) {
                  var s = o.index();
                  e.slideTo(s, 0, e.params.runCallbacksOnInit, !0);
                }
              }
            e.params.hashNavigation.watchState &&
              $(n).on("hashchange", e.hashNavigation.onHashChange);
          }
        },
        destroy: function destroy() {
          var e = getWindow();
          this.params.hashNavigation.watchState &&
            $(e).off("hashchange", this.hashNavigation.onHashChange);
        },
      };
      function _extends$5() {
        return (
          (_extends$5 =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$5.apply(this, arguments)
        );
      }
      var J = {
        run: function run() {
          var e = this,
            t = e.slides.eq(e.activeIndex),
            n = e.params.autoplay.delay;
          t.attr("data-swiper-autoplay") &&
            (n = t.attr("data-swiper-autoplay") || e.params.autoplay.delay),
            clearTimeout(e.autoplay.timeout),
            (e.autoplay.timeout = nextTick(function () {
              var t;
              e.params.autoplay.reverseDirection
                ? e.params.loop
                  ? (e.loopFix(),
                    (t = e.slidePrev(e.params.speed, !0, !0)),
                    e.emit("autoplay"))
                  : e.isBeginning
                  ? e.params.autoplay.stopOnLastSlide
                    ? e.autoplay.stop()
                    : ((t = e.slideTo(
                        e.slides.length - 1,
                        e.params.speed,
                        !0,
                        !0
                      )),
                      e.emit("autoplay"))
                  : ((t = e.slidePrev(e.params.speed, !0, !0)),
                    e.emit("autoplay"))
                : e.params.loop
                ? (e.loopFix(),
                  (t = e.slideNext(e.params.speed, !0, !0)),
                  e.emit("autoplay"))
                : e.isEnd
                ? e.params.autoplay.stopOnLastSlide
                  ? e.autoplay.stop()
                  : ((t = e.slideTo(0, e.params.speed, !0, !0)),
                    e.emit("autoplay"))
                : ((t = e.slideNext(e.params.speed, !0, !0)),
                  e.emit("autoplay")),
                ((e.params.cssMode && e.autoplay.running) || !1 === t) &&
                  e.autoplay.run();
            }, n));
        },
        start: function start() {
          var e = this;
          return (
            void 0 === e.autoplay.timeout &&
            !e.autoplay.running &&
            ((e.autoplay.running = !0),
            e.emit("autoplayStart"),
            e.autoplay.run(),
            !0)
          );
        },
        stop: function stop() {
          var e = this;
          return (
            !!e.autoplay.running &&
            void 0 !== e.autoplay.timeout &&
            (e.autoplay.timeout &&
              (clearTimeout(e.autoplay.timeout), (e.autoplay.timeout = void 0)),
            (e.autoplay.running = !1),
            e.emit("autoplayStop"),
            !0)
          );
        },
        pause: function pause(e) {
          var t = this;
          t.autoplay.running &&
            (t.autoplay.paused ||
              (t.autoplay.timeout && clearTimeout(t.autoplay.timeout),
              (t.autoplay.paused = !0),
              0 !== e && t.params.autoplay.waitForTransition
                ? ["transitionend", "webkitTransitionEnd"].forEach(function (
                    e
                  ) {
                    t.$wrapperEl[0].addEventListener(
                      e,
                      t.autoplay.onTransitionEnd
                    );
                  })
                : ((t.autoplay.paused = !1), t.autoplay.run())));
        },
        onVisibilityChange: function onVisibilityChange() {
          var e = this,
            t = getDocument();
          "hidden" === t.visibilityState &&
            e.autoplay.running &&
            e.autoplay.pause(),
            "visible" === t.visibilityState &&
              e.autoplay.paused &&
              (e.autoplay.run(), (e.autoplay.paused = !1));
        },
        onTransitionEnd: function onTransitionEnd(e) {
          var t = this;
          t &&
            !t.destroyed &&
            t.$wrapperEl &&
            e.target === t.$wrapperEl[0] &&
            (["transitionend", "webkitTransitionEnd"].forEach(function (e) {
              t.$wrapperEl[0].removeEventListener(
                e,
                t.autoplay.onTransitionEnd
              );
            }),
            (t.autoplay.paused = !1),
            t.autoplay.running ? t.autoplay.run() : t.autoplay.stop());
        },
        onMouseEnter: function onMouseEnter() {
          var e = this;
          e.params.autoplay.disableOnInteraction
            ? e.autoplay.stop()
            : e.autoplay.pause(),
            ["transitionend", "webkitTransitionEnd"].forEach(function (t) {
              e.$wrapperEl[0].removeEventListener(
                t,
                e.autoplay.onTransitionEnd
              );
            });
        },
        onMouseLeave: function onMouseLeave() {
          var e = this;
          e.params.autoplay.disableOnInteraction ||
            ((e.autoplay.paused = !1), e.autoplay.run());
        },
        attachMouseEvents: function attachMouseEvents() {
          var e = this;
          e.params.autoplay.pauseOnMouseEnter &&
            (e.$el.on("mouseenter", e.autoplay.onMouseEnter),
            e.$el.on("mouseleave", e.autoplay.onMouseLeave));
        },
        detachMouseEvents: function detachMouseEvents() {
          var e = this;
          e.$el.off("mouseenter", e.autoplay.onMouseEnter),
            e.$el.off("mouseleave", e.autoplay.onMouseLeave);
        },
      };
      function _extends$4() {
        return (
          (_extends$4 =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$4.apply(this, arguments)
        );
      }
      var K = {
        setTranslate: function setTranslate() {
          for (var e = this, t = e.slides, n = 0; n < t.length; n += 1) {
            var i = e.slides.eq(n),
              r = -i[0].swiperSlideOffset;
            e.params.virtualTranslate || (r -= e.translate);
            var a = 0;
            e.isHorizontal() || ((a = r), (r = 0));
            var o = e.params.fadeEffect.crossFade
              ? Math.max(1 - Math.abs(i[0].progress), 0)
              : 1 + Math.min(Math.max(i[0].progress, -1), 0);
            i.css({
              opacity: o,
            }).transform("translate3d(" + r + "px, " + a + "px, 0px)");
          }
        },
        setTransition: function setTransition(e) {
          var t = this,
            n = t.slides,
            i = t.$wrapperEl;
          if ((n.transition(e), t.params.virtualTranslate && 0 !== e)) {
            var r = !1;
            n.transitionEnd(function () {
              if (!r && t && !t.destroyed) {
                (r = !0), (t.animating = !1);
                for (
                  var e = ["webkitTransitionEnd", "transitionend"], n = 0;
                  n < e.length;
                  n += 1
                )
                  i.trigger(e[n]);
              }
            });
          }
        },
      };
      function _extends$3() {
        return (
          (_extends$3 =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$3.apply(this, arguments)
        );
      }
      var ee = {
        setTranslate: function setTranslate() {
          var e,
            t = this,
            n = t.$el,
            i = t.$wrapperEl,
            r = t.slides,
            a = t.width,
            o = t.height,
            s = t.rtlTranslate,
            l = t.size,
            c = t.browser,
            d = t.params.cubeEffect,
            u = t.isHorizontal(),
            p = t.virtual && t.params.virtual.enabled,
            h = 0;
          d.shadow &&
            (u
              ? (0 === (e = i.find(".swiper-cube-shadow")).length &&
                  ((e = $('<div class="swiper-cube-shadow"></div>')),
                  i.append(e)),
                e.css({
                  height: a + "px",
                }))
              : 0 === (e = n.find(".swiper-cube-shadow")).length &&
                ((e = $('<div class="swiper-cube-shadow"></div>')),
                n.append(e)));
          for (var m = 0; m < r.length; m += 1) {
            var f = r.eq(m),
              v = m;
            p && (v = parseInt(f.attr("data-swiper-slide-index"), 10));
            var g = 90 * v,
              w = Math.floor(g / 360);
            s && ((g = -g), (w = Math.floor(-g / 360)));
            var b = Math.max(Math.min(f[0].progress, 1), -1),
              y = 0,
              A = 0,
              x = 0;
            v % 4 == 0
              ? ((y = 4 * -w * l), (x = 0))
              : (v - 1) % 4 == 0
              ? ((y = 0), (x = 4 * -w * l))
              : (v - 2) % 4 == 0
              ? ((y = l + 4 * w * l), (x = l))
              : (v - 3) % 4 == 0 && ((y = -l), (x = 3 * l + 4 * l * w)),
              s && (y = -y),
              u || ((A = y), (y = 0));
            var _ =
              "rotateX(" +
              (u ? 0 : -g) +
              "deg) rotateY(" +
              (u ? g : 0) +
              "deg) translate3d(" +
              y +
              "px, " +
              A +
              "px, " +
              x +
              "px)";
            if (
              (b <= 1 &&
                b > -1 &&
                ((h = 90 * v + 90 * b), s && (h = 90 * -v - 90 * b)),
              f.transform(_),
              d.slideShadows)
            ) {
              var E = u
                  ? f.find(".swiper-slide-shadow-left")
                  : f.find(".swiper-slide-shadow-top"),
                S = u
                  ? f.find(".swiper-slide-shadow-right")
                  : f.find(".swiper-slide-shadow-bottom");
              0 === E.length &&
                ((E = $(
                  '<div class="swiper-slide-shadow-' +
                    (u ? "left" : "top") +
                    '"></div>'
                )),
                f.append(E)),
                0 === S.length &&
                  ((S = $(
                    '<div class="swiper-slide-shadow-' +
                      (u ? "right" : "bottom") +
                      '"></div>'
                  )),
                  f.append(S)),
                E.length && (E[0].style.opacity = Math.max(-b, 0)),
                S.length && (S[0].style.opacity = Math.max(b, 0));
            }
          }
          if (
            (i.css({
              "-webkit-transform-origin": "50% 50% -" + l / 2 + "px",
              "-moz-transform-origin": "50% 50% -" + l / 2 + "px",
              "-ms-transform-origin": "50% 50% -" + l / 2 + "px",
              "transform-origin": "50% 50% -" + l / 2 + "px",
            }),
            d.shadow)
          )
            if (u)
              e.transform(
                "translate3d(0px, " +
                  (a / 2 + d.shadowOffset) +
                  "px, " +
                  -a / 2 +
                  "px) rotateX(90deg) rotateZ(0deg) scale(" +
                  d.shadowScale +
                  ")"
              );
            else {
              var C = Math.abs(h) - 90 * Math.floor(Math.abs(h) / 90),
                T =
                  1.5 -
                  (Math.sin((2 * C * Math.PI) / 360) / 2 +
                    Math.cos((2 * C * Math.PI) / 360) / 2),
                k = d.shadowScale,
                M = d.shadowScale / T,
                I = d.shadowOffset;
              e.transform(
                "scale3d(" +
                  k +
                  ", 1, " +
                  M +
                  ") translate3d(0px, " +
                  (o / 2 + I) +
                  "px, " +
                  -o / 2 / M +
                  "px) rotateX(-90deg)"
              );
            }
          var L = c.isSafari || c.isWebView ? -l / 2 : 0;
          i.transform(
            "translate3d(0px,0," +
              L +
              "px) rotateX(" +
              (t.isHorizontal() ? 0 : h) +
              "deg) rotateY(" +
              (t.isHorizontal() ? -h : 0) +
              "deg)"
          );
        },
        setTransition: function setTransition(e) {
          var t = this,
            n = t.$el;
          t.slides
            .transition(e)
            .find(
              ".swiper-slide-shadow-top, .swiper-slide-shadow-right, .swiper-slide-shadow-bottom, .swiper-slide-shadow-left"
            )
            .transition(e),
            t.params.cubeEffect.shadow &&
              !t.isHorizontal() &&
              n.find(".swiper-cube-shadow").transition(e);
        },
      };
      function _extends$2() {
        return (
          (_extends$2 =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$2.apply(this, arguments)
        );
      }
      var te = {
        setTranslate: function setTranslate() {
          for (
            var e = this, t = e.slides, n = e.rtlTranslate, i = 0;
            i < t.length;
            i += 1
          ) {
            var r = t.eq(i),
              a = r[0].progress;
            e.params.flipEffect.limitRotation &&
              (a = Math.max(Math.min(r[0].progress, 1), -1));
            var o = -180 * a,
              s = 0,
              l = -r[0].swiperSlideOffset,
              c = 0;
            if (
              (e.isHorizontal()
                ? n && (o = -o)
                : ((c = l), (l = 0), (s = -o), (o = 0)),
              (r[0].style.zIndex = -Math.abs(Math.round(a)) + t.length),
              e.params.flipEffect.slideShadows)
            ) {
              var d = e.isHorizontal()
                  ? r.find(".swiper-slide-shadow-left")
                  : r.find(".swiper-slide-shadow-top"),
                u = e.isHorizontal()
                  ? r.find(".swiper-slide-shadow-right")
                  : r.find(".swiper-slide-shadow-bottom");
              0 === d.length &&
                ((d = $(
                  '<div class="swiper-slide-shadow-' +
                    (e.isHorizontal() ? "left" : "top") +
                    '"></div>'
                )),
                r.append(d)),
                0 === u.length &&
                  ((u = $(
                    '<div class="swiper-slide-shadow-' +
                      (e.isHorizontal() ? "right" : "bottom") +
                      '"></div>'
                  )),
                  r.append(u)),
                d.length && (d[0].style.opacity = Math.max(-a, 0)),
                u.length && (u[0].style.opacity = Math.max(a, 0));
            }
            r.transform(
              "translate3d(" +
                l +
                "px, " +
                c +
                "px, 0px) rotateX(" +
                s +
                "deg) rotateY(" +
                o +
                "deg)"
            );
          }
        },
        setTransition: function setTransition(e) {
          var t = this,
            n = t.slides,
            i = t.activeIndex,
            r = t.$wrapperEl;
          if (
            (n
              .transition(e)
              .find(
                ".swiper-slide-shadow-top, .swiper-slide-shadow-right, .swiper-slide-shadow-bottom, .swiper-slide-shadow-left"
              )
              .transition(e),
            t.params.virtualTranslate && 0 !== e)
          ) {
            var a = !1;
            n.eq(i).transitionEnd(function onTransitionEnd() {
              if (!a && t && !t.destroyed) {
                (a = !0), (t.animating = !1);
                for (
                  var e = ["webkitTransitionEnd", "transitionend"], n = 0;
                  n < e.length;
                  n += 1
                )
                  r.trigger(e[n]);
              }
            });
          }
        },
      };
      function _extends$1() {
        return (
          (_extends$1 =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends$1.apply(this, arguments)
        );
      }
      var ne = {
        setTranslate: function setTranslate() {
          for (
            var e = this,
              t = e.width,
              n = e.height,
              i = e.slides,
              r = e.slidesSizesGrid,
              a = e.params.coverflowEffect,
              o = e.isHorizontal(),
              s = e.translate,
              l = o ? t / 2 - s : n / 2 - s,
              c = o ? a.rotate : -a.rotate,
              d = a.depth,
              u = 0,
              p = i.length;
            u < p;
            u += 1
          ) {
            var h = i.eq(u),
              m = r[u],
              f = ((l - h[0].swiperSlideOffset - m / 2) / m) * a.modifier,
              v = o ? c * f : 0,
              g = o ? 0 : c * f,
              w = -d * Math.abs(f),
              b = a.stretch;
            "string" == typeof b &&
              -1 !== b.indexOf("%") &&
              (b = (parseFloat(a.stretch) / 100) * m);
            var y = o ? 0 : b * f,
              A = o ? b * f : 0,
              x = 1 - (1 - a.scale) * Math.abs(f);
            Math.abs(A) < 0.001 && (A = 0),
              Math.abs(y) < 0.001 && (y = 0),
              Math.abs(w) < 0.001 && (w = 0),
              Math.abs(v) < 0.001 && (v = 0),
              Math.abs(g) < 0.001 && (g = 0),
              Math.abs(x) < 0.001 && (x = 0);
            var _ =
              "translate3d(" +
              A +
              "px," +
              y +
              "px," +
              w +
              "px)  rotateX(" +
              g +
              "deg) rotateY(" +
              v +
              "deg) scale(" +
              x +
              ")";
            if (
              (h.transform(_),
              (h[0].style.zIndex = 1 - Math.abs(Math.round(f))),
              a.slideShadows)
            ) {
              var E = o
                  ? h.find(".swiper-slide-shadow-left")
                  : h.find(".swiper-slide-shadow-top"),
                S = o
                  ? h.find(".swiper-slide-shadow-right")
                  : h.find(".swiper-slide-shadow-bottom");
              0 === E.length &&
                ((E = $(
                  '<div class="swiper-slide-shadow-' +
                    (o ? "left" : "top") +
                    '"></div>'
                )),
                h.append(E)),
                0 === S.length &&
                  ((S = $(
                    '<div class="swiper-slide-shadow-' +
                      (o ? "right" : "bottom") +
                      '"></div>'
                  )),
                  h.append(S)),
                E.length && (E[0].style.opacity = f > 0 ? f : 0),
                S.length && (S[0].style.opacity = -f > 0 ? -f : 0);
            }
          }
        },
        setTransition: function setTransition(e) {
          this.slides
            .transition(e)
            .find(
              ".swiper-slide-shadow-top, .swiper-slide-shadow-right, .swiper-slide-shadow-bottom, .swiper-slide-shadow-left"
            )
            .transition(e);
        },
      };
      function _extends() {
        return (
          (_extends =
            Object.assign ||
            function (e) {
              for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var i in n)
                  Object.prototype.hasOwnProperty.call(n, i) && (e[i] = n[i]);
              }
              return e;
            }),
          _extends.apply(this, arguments)
        );
      }
      var ie = {
          init: function init() {
            var e = this,
              t = e.params.thumbs;
            if (e.thumbs.initialized) return !1;
            e.thumbs.initialized = !0;
            var n = e.constructor;
            return (
              t.swiper instanceof n
                ? ((e.thumbs.swiper = t.swiper),
                  extend(e.thumbs.swiper.originalParams, {
                    watchSlidesProgress: !0,
                    slideToClickedSlide: !1,
                  }),
                  extend(e.thumbs.swiper.params, {
                    watchSlidesProgress: !0,
                    slideToClickedSlide: !1,
                  }))
                : isObject(t.swiper) &&
                  ((e.thumbs.swiper = new n(
                    extend({}, t.swiper, {
                      watchSlidesVisibility: !0,
                      watchSlidesProgress: !0,
                      slideToClickedSlide: !1,
                    })
                  )),
                  (e.thumbs.swiperCreated = !0)),
              e.thumbs.swiper.$el.addClass(
                e.params.thumbs.thumbsContainerClass
              ),
              e.thumbs.swiper.on("tap", e.thumbs.onThumbClick),
              !0
            );
          },
          onThumbClick: function onThumbClick() {
            var e = this,
              t = e.thumbs.swiper;
            if (t) {
              var n = t.clickedIndex,
                i = t.clickedSlide;
              if (
                !(
                  (i && $(i).hasClass(e.params.thumbs.slideThumbActiveClass)) ||
                  null == n
                )
              ) {
                var r;
                if (
                  ((r = t.params.loop
                    ? parseInt(
                        $(t.clickedSlide).attr("data-swiper-slide-index"),
                        10
                      )
                    : n),
                  e.params.loop)
                ) {
                  var a = e.activeIndex;
                  e.slides.eq(a).hasClass(e.params.slideDuplicateClass) &&
                    (e.loopFix(),
                    (e._clientLeft = e.$wrapperEl[0].clientLeft),
                    (a = e.activeIndex));
                  var o = e.slides
                      .eq(a)
                      .prevAll('[data-swiper-slide-index="' + r + '"]')
                      .eq(0)
                      .index(),
                    s = e.slides
                      .eq(a)
                      .nextAll('[data-swiper-slide-index="' + r + '"]')
                      .eq(0)
                      .index();
                  r =
                    void 0 === o ? s : void 0 === s ? o : s - a < a - o ? s : o;
                }
                e.slideTo(r);
              }
            }
          },
          update: function update(e) {
            var t = this,
              n = t.thumbs.swiper;
            if (n) {
              var i =
                  "auto" === n.params.slidesPerView
                    ? n.slidesPerViewDynamic()
                    : n.params.slidesPerView,
                r = t.params.thumbs.autoScrollOffset,
                a = r && !n.params.loop;
              if (t.realIndex !== n.realIndex || a) {
                var o,
                  s,
                  l = n.activeIndex;
                if (n.params.loop) {
                  n.slides.eq(l).hasClass(n.params.slideDuplicateClass) &&
                    (n.loopFix(),
                    (n._clientLeft = n.$wrapperEl[0].clientLeft),
                    (l = n.activeIndex));
                  var c = n.slides
                      .eq(l)
                      .prevAll(
                        '[data-swiper-slide-index="' + t.realIndex + '"]'
                      )
                      .eq(0)
                      .index(),
                    d = n.slides
                      .eq(l)
                      .nextAll(
                        '[data-swiper-slide-index="' + t.realIndex + '"]'
                      )
                      .eq(0)
                      .index();
                  (o =
                    void 0 === c
                      ? d
                      : void 0 === d
                      ? c
                      : d - l == l - c
                      ? n.params.slidesPerGroup > 1
                        ? d
                        : l
                      : d - l < l - c
                      ? d
                      : c),
                    (s = t.activeIndex > t.previousIndex ? "next" : "prev");
                } else
                  s = (o = t.realIndex) > t.previousIndex ? "next" : "prev";
                a && (o += "next" === s ? r : -1 * r),
                  n.visibleSlidesIndexes &&
                    n.visibleSlidesIndexes.indexOf(o) < 0 &&
                    (n.params.centeredSlides &&
                      (o =
                        o > l
                          ? o - Math.floor(i / 2) + 1
                          : o + Math.floor(i / 2) - 1),
                    n.slideTo(o, e ? 0 : void 0));
              }
              var u = 1,
                p = t.params.thumbs.slideThumbActiveClass;
              if (
                (t.params.slidesPerView > 1 &&
                  !t.params.centeredSlides &&
                  (u = t.params.slidesPerView),
                t.params.thumbs.multipleActiveThumbs || (u = 1),
                (u = Math.floor(u)),
                n.slides.removeClass(p),
                n.params.loop || (n.params.virtual && n.params.virtual.enabled))
              )
                for (var h = 0; h < u; h += 1)
                  n.$wrapperEl
                    .children(
                      '[data-swiper-slide-index="' + (t.realIndex + h) + '"]'
                    )
                    .addClass(p);
              else
                for (var m = 0; m < u; m += 1)
                  n.slides.eq(t.realIndex + m).addClass(p);
            }
          },
        },
        re = [
          z,
          B,
          {
            name: "mousewheel",
            params: {
              mousewheel: {
                enabled: !1,
                releaseOnEdges: !1,
                invert: !1,
                forceToAxis: !1,
                sensitivity: 1,
                eventsTarget: "container",
                thresholdDelta: null,
                thresholdTime: null,
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                mousewheel: {
                  enabled: !1,
                  lastScrollTime: now(),
                  lastEventBeforeSnap: void 0,
                  recentWheelEvents: [],
                  enable: R.enable,
                  disable: R.disable,
                  handle: R.handle,
                  handleMouseEnter: R.handleMouseEnter,
                  handleMouseLeave: R.handleMouseLeave,
                  animateSlider: R.animateSlider,
                  releaseScroll: R.releaseScroll,
                },
              });
            },
            on: {
              init: function init(e) {
                !e.params.mousewheel.enabled &&
                  e.params.cssMode &&
                  e.mousewheel.disable(),
                  e.params.mousewheel.enabled && e.mousewheel.enable();
              },
              destroy: function destroy(e) {
                e.params.cssMode && e.mousewheel.enable(),
                  e.mousewheel.enabled && e.mousewheel.disable();
              },
            },
          },
          {
            name: "navigation",
            params: {
              navigation: {
                nextEl: null,
                prevEl: null,
                hideOnClick: !1,
                disabledClass: "swiper-button-disabled",
                hiddenClass: "swiper-button-hidden",
                lockClass: "swiper-button-lock",
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                navigation: _extends$f({}, F),
              });
            },
            on: {
              init: function init(e) {
                e.navigation.init(), e.navigation.update();
              },
              toEdge: function toEdge(e) {
                e.navigation.update();
              },
              fromEdge: function fromEdge(e) {
                e.navigation.update();
              },
              destroy: function destroy(e) {
                e.navigation.destroy();
              },
              "enable disable": function enableDisable(e) {
                var t = e.navigation,
                  n = t.$nextEl,
                  i = t.$prevEl;
                n &&
                  n[e.enabled ? "removeClass" : "addClass"](
                    e.params.navigation.lockClass
                  ),
                  i &&
                    i[e.enabled ? "removeClass" : "addClass"](
                      e.params.navigation.lockClass
                    );
              },
              click: function click(e, t) {
                var n = e.navigation,
                  i = n.$nextEl,
                  r = n.$prevEl,
                  a = t.target;
                if (
                  e.params.navigation.hideOnClick &&
                  !$(a).is(r) &&
                  !$(a).is(i)
                ) {
                  if (
                    e.pagination &&
                    e.params.pagination &&
                    e.params.pagination.clickable &&
                    (e.pagination.el === a || e.pagination.el.contains(a))
                  )
                    return;
                  var o;
                  i
                    ? (o = i.hasClass(e.params.navigation.hiddenClass))
                    : r && (o = r.hasClass(e.params.navigation.hiddenClass)),
                    !0 === o
                      ? e.emit("navigationShow")
                      : e.emit("navigationHide"),
                    i && i.toggleClass(e.params.navigation.hiddenClass),
                    r && r.toggleClass(e.params.navigation.hiddenClass);
                }
              },
            },
          },
          {
            name: "pagination",
            params: {
              pagination: {
                el: null,
                bulletElement: "span",
                clickable: !1,
                hideOnClick: !1,
                renderBullet: null,
                renderProgressbar: null,
                renderFraction: null,
                renderCustom: null,
                progressbarOpposite: !1,
                type: "bullets",
                dynamicBullets: !1,
                dynamicMainBullets: 1,
                formatFractionCurrent: function formatFractionCurrent(e) {
                  return e;
                },
                formatFractionTotal: function formatFractionTotal(e) {
                  return e;
                },
                bulletClass: "swiper-pagination-bullet",
                bulletActiveClass: "swiper-pagination-bullet-active",
                modifierClass: "swiper-pagination-",
                currentClass: "swiper-pagination-current",
                totalClass: "swiper-pagination-total",
                hiddenClass: "swiper-pagination-hidden",
                progressbarFillClass: "swiper-pagination-progressbar-fill",
                progressbarOppositeClass:
                  "swiper-pagination-progressbar-opposite",
                clickableClass: "swiper-pagination-clickable",
                lockClass: "swiper-pagination-lock",
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                pagination: _extends$e(
                  {
                    dynamicBulletIndex: 0,
                  },
                  Y
                ),
              });
            },
            on: {
              init: function init(e) {
                e.pagination.init(),
                  e.pagination.render(),
                  e.pagination.update();
              },
              activeIndexChange: function activeIndexChange(e) {
                (e.params.loop || void 0 === e.snapIndex) &&
                  e.pagination.update();
              },
              snapIndexChange: function snapIndexChange(e) {
                e.params.loop || e.pagination.update();
              },
              slidesLengthChange: function slidesLengthChange(e) {
                e.params.loop && (e.pagination.render(), e.pagination.update());
              },
              snapGridLengthChange: function snapGridLengthChange(e) {
                e.params.loop || (e.pagination.render(), e.pagination.update());
              },
              destroy: function destroy(e) {
                e.pagination.destroy();
              },
              "enable disable": function enableDisable(e) {
                var t = e.pagination.$el;
                t &&
                  t[e.enabled ? "removeClass" : "addClass"](
                    e.params.pagination.lockClass
                  );
              },
              click: function click(e, t) {
                var n = t.target;
                if (
                  e.params.pagination.el &&
                  e.params.pagination.hideOnClick &&
                  e.pagination.$el.length > 0 &&
                  !$(n).hasClass(e.params.pagination.bulletClass)
                ) {
                  if (
                    e.navigation &&
                    ((e.navigation.nextEl && n === e.navigation.nextEl) ||
                      (e.navigation.prevEl && n === e.navigation.prevEl))
                  )
                    return;
                  !0 ===
                  e.pagination.$el.hasClass(e.params.pagination.hiddenClass)
                    ? e.emit("paginationShow")
                    : e.emit("paginationHide"),
                    e.pagination.$el.toggleClass(
                      e.params.pagination.hiddenClass
                    );
                }
              },
            },
          },
          {
            name: "scrollbar",
            params: {
              scrollbar: {
                el: null,
                dragSize: "auto",
                hide: !1,
                draggable: !1,
                snapOnRelease: !0,
                lockClass: "swiper-scrollbar-lock",
                dragClass: "swiper-scrollbar-drag",
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                scrollbar: _extends$d(
                  {
                    isTouched: !1,
                    timeout: null,
                    dragTimeout: null,
                  },
                  G
                ),
              });
            },
            on: {
              init: function init(e) {
                e.scrollbar.init(),
                  e.scrollbar.updateSize(),
                  e.scrollbar.setTranslate();
              },
              update: function update(e) {
                e.scrollbar.updateSize();
              },
              resize: function resize(e) {
                e.scrollbar.updateSize();
              },
              observerUpdate: function observerUpdate(e) {
                e.scrollbar.updateSize();
              },
              setTranslate: function setTranslate(e) {
                e.scrollbar.setTranslate();
              },
              setTransition: function setTransition(e, t) {
                e.scrollbar.setTransition(t);
              },
              "enable disable": function enableDisable(e) {
                var t = e.scrollbar.$el;
                t &&
                  t[e.enabled ? "removeClass" : "addClass"](
                    e.params.scrollbar.lockClass
                  );
              },
              destroy: function destroy(e) {
                e.scrollbar.destroy();
              },
            },
          },
          {
            name: "parallax",
            params: {
              parallax: {
                enabled: !1,
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                parallax: _extends$c({}, V),
              });
            },
            on: {
              beforeInit: function beforeInit(e) {
                e.params.parallax.enabled &&
                  ((e.params.watchSlidesProgress = !0),
                  (e.originalParams.watchSlidesProgress = !0));
              },
              init: function init(e) {
                e.params.parallax.enabled && e.parallax.setTranslate();
              },
              setTranslate: function setTranslate(e) {
                e.params.parallax.enabled && e.parallax.setTranslate();
              },
              setTransition: function setTransition(e, t) {
                e.params.parallax.enabled && e.parallax.setTransition(t);
              },
            },
          },
          {
            name: "zoom",
            params: {
              zoom: {
                enabled: !1,
                maxRatio: 3,
                minRatio: 1,
                toggle: !0,
                containerClass: "swiper-zoom-container",
                zoomedSlideClass: "swiper-slide-zoomed",
              },
            },
            create: function create() {
              var e = this;
              bindModuleMethods(e, {
                zoom: _extends$b(
                  {
                    enabled: !1,
                    scale: 1,
                    currentScale: 1,
                    isScaling: !1,
                    gesture: {
                      $slideEl: void 0,
                      slideWidth: void 0,
                      slideHeight: void 0,
                      $imageEl: void 0,
                      $imageWrapEl: void 0,
                      maxRatio: 3,
                    },
                    image: {
                      isTouched: void 0,
                      isMoved: void 0,
                      currentX: void 0,
                      currentY: void 0,
                      minX: void 0,
                      minY: void 0,
                      maxX: void 0,
                      maxY: void 0,
                      width: void 0,
                      height: void 0,
                      startX: void 0,
                      startY: void 0,
                      touchesStart: {},
                      touchesCurrent: {},
                    },
                    velocity: {
                      x: void 0,
                      y: void 0,
                      prevPositionX: void 0,
                      prevPositionY: void 0,
                      prevTime: void 0,
                    },
                  },
                  W
                ),
              });
              var t = 1;
              Object.defineProperty(e.zoom, "scale", {
                get: function get() {
                  return t;
                },
                set: function set(n) {
                  if (t !== n) {
                    var i = e.zoom.gesture.$imageEl
                        ? e.zoom.gesture.$imageEl[0]
                        : void 0,
                      r = e.zoom.gesture.$slideEl
                        ? e.zoom.gesture.$slideEl[0]
                        : void 0;
                    e.emit("zoomChange", n, i, r);
                  }
                  t = n;
                },
              });
            },
            on: {
              init: function init(e) {
                e.params.zoom.enabled && e.zoom.enable();
              },
              destroy: function destroy(e) {
                e.zoom.disable();
              },
              touchStart: function touchStart(e, t) {
                e.zoom.enabled && e.zoom.onTouchStart(t);
              },
              touchEnd: function touchEnd(e, t) {
                e.zoom.enabled && e.zoom.onTouchEnd(t);
              },
              doubleTap: function doubleTap(e, t) {
                !e.animating &&
                  e.params.zoom.enabled &&
                  e.zoom.enabled &&
                  e.params.zoom.toggle &&
                  e.zoom.toggle(t);
              },
              transitionEnd: function transitionEnd(e) {
                e.zoom.enabled &&
                  e.params.zoom.enabled &&
                  e.zoom.onTransitionEnd();
              },
              slideChange: function slideChange(e) {
                e.zoom.enabled &&
                  e.params.zoom.enabled &&
                  e.params.cssMode &&
                  e.zoom.onTransitionEnd();
              },
            },
          },
          {
            name: "lazy",
            params: {
              lazy: {
                checkInView: !1,
                enabled: !1,
                loadPrevNext: !1,
                loadPrevNextAmount: 1,
                loadOnTransitionStart: !1,
                scrollingElement: "",
                elementClass: "swiper-lazy",
                loadingClass: "swiper-lazy-loading",
                loadedClass: "swiper-lazy-loaded",
                preloaderClass: "swiper-lazy-preloader",
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                lazy: _extends$a(
                  {
                    initialImageLoaded: !1,
                  },
                  H
                ),
              });
            },
            on: {
              beforeInit: function beforeInit(e) {
                e.params.lazy.enabled &&
                  e.params.preloadImages &&
                  (e.params.preloadImages = !1);
              },
              init: function init(e) {
                e.params.lazy.enabled &&
                  !e.params.loop &&
                  0 === e.params.initialSlide &&
                  (e.params.lazy.checkInView
                    ? e.lazy.checkInViewOnLoad()
                    : e.lazy.load());
              },
              scroll: function scroll(e) {
                e.params.freeMode && !e.params.freeModeSticky && e.lazy.load();
              },
              "scrollbarDragMove resize _freeModeNoMomentumRelease":
                function lazyLoad(e) {
                  e.params.lazy.enabled && e.lazy.load();
                },
              transitionStart: function transitionStart(e) {
                e.params.lazy.enabled &&
                  (e.params.lazy.loadOnTransitionStart ||
                    (!e.params.lazy.loadOnTransitionStart &&
                      !e.lazy.initialImageLoaded)) &&
                  e.lazy.load();
              },
              transitionEnd: function transitionEnd(e) {
                e.params.lazy.enabled &&
                  !e.params.lazy.loadOnTransitionStart &&
                  e.lazy.load();
              },
              slideChange: function slideChange(e) {
                var t = e.params,
                  n = t.lazy,
                  i = t.cssMode,
                  r = t.watchSlidesVisibility,
                  a = t.watchSlidesProgress,
                  o = t.touchReleaseOnEdges,
                  s = t.resistanceRatio;
                n.enabled &&
                  (i || ((r || a) && (o || 0 === s))) &&
                  e.lazy.load();
              },
            },
          },
          U,
          {
            name: "a11y",
            params: {
              a11y: {
                enabled: !0,
                notificationClass: "swiper-notification",
                prevSlideMessage: "Previous slide",
                nextSlideMessage: "Next slide",
                firstSlideMessage: "This is the first slide",
                lastSlideMessage: "This is the last slide",
                paginationBulletMessage: "Go to slide {{index}}",
                slideLabelMessage: "{{index}} / {{slidesLength}}",
                containerMessage: null,
                containerRoleDescriptionMessage: null,
                itemRoleDescriptionMessage: null,
                slideRole: "group",
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                a11y: _extends$8({}, Z, {
                  liveRegion: $(
                    '<span class="' +
                      this.params.a11y.notificationClass +
                      '" aria-live="assertive" aria-atomic="true"></span>'
                  ),
                }),
              });
            },
            on: {
              afterInit: function afterInit(e) {
                e.params.a11y.enabled &&
                  (e.a11y.init(), e.a11y.updateNavigation());
              },
              toEdge: function toEdge(e) {
                e.params.a11y.enabled && e.a11y.updateNavigation();
              },
              fromEdge: function fromEdge(e) {
                e.params.a11y.enabled && e.a11y.updateNavigation();
              },
              paginationUpdate: function paginationUpdate(e) {
                e.params.a11y.enabled && e.a11y.updatePagination();
              },
              destroy: function destroy(e) {
                e.params.a11y.enabled && e.a11y.destroy();
              },
            },
          },
          {
            name: "history",
            params: {
              history: {
                enabled: !1,
                root: "",
                replaceState: !1,
                key: "slides",
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                history: _extends$7({}, Q),
              });
            },
            on: {
              init: function init(e) {
                e.params.history.enabled && e.history.init();
              },
              destroy: function destroy(e) {
                e.params.history.enabled && e.history.destroy();
              },
              "transitionEnd _freeModeNoMomentumRelease":
                function transitionEnd_freeModeNoMomentumRelease(e) {
                  e.history.initialized &&
                    e.history.setHistory(e.params.history.key, e.activeIndex);
                },
              slideChange: function slideChange(e) {
                e.history.initialized &&
                  e.params.cssMode &&
                  e.history.setHistory(e.params.history.key, e.activeIndex);
              },
            },
          },
          {
            name: "hash-navigation",
            params: {
              hashNavigation: {
                enabled: !1,
                replaceState: !1,
                watchState: !1,
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                hashNavigation: _extends$6(
                  {
                    initialized: !1,
                  },
                  q
                ),
              });
            },
            on: {
              init: function init(e) {
                e.params.hashNavigation.enabled && e.hashNavigation.init();
              },
              destroy: function destroy(e) {
                e.params.hashNavigation.enabled && e.hashNavigation.destroy();
              },
              "transitionEnd _freeModeNoMomentumRelease":
                function transitionEnd_freeModeNoMomentumRelease(e) {
                  e.hashNavigation.initialized && e.hashNavigation.setHash();
                },
              slideChange: function slideChange(e) {
                e.hashNavigation.initialized &&
                  e.params.cssMode &&
                  e.hashNavigation.setHash();
              },
            },
          },
          {
            name: "autoplay",
            params: {
              autoplay: {
                enabled: !1,
                delay: 3e3,
                waitForTransition: !0,
                disableOnInteraction: !0,
                stopOnLastSlide: !1,
                reverseDirection: !1,
                pauseOnMouseEnter: !1,
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                autoplay: _extends$5({}, J, {
                  running: !1,
                  paused: !1,
                }),
              });
            },
            on: {
              init: function init(e) {
                e.params.autoplay.enabled &&
                  (e.autoplay.start(),
                  getDocument().addEventListener(
                    "visibilitychange",
                    e.autoplay.onVisibilityChange
                  ),
                  e.autoplay.attachMouseEvents());
              },
              beforeTransitionStart: function beforeTransitionStart(e, t, n) {
                e.autoplay.running &&
                  (n || !e.params.autoplay.disableOnInteraction
                    ? e.autoplay.pause(t)
                    : e.autoplay.stop());
              },
              sliderFirstMove: function sliderFirstMove(e) {
                e.autoplay.running &&
                  (e.params.autoplay.disableOnInteraction
                    ? e.autoplay.stop()
                    : e.autoplay.pause());
              },
              touchEnd: function touchEnd(e) {
                e.params.cssMode &&
                  e.autoplay.paused &&
                  !e.params.autoplay.disableOnInteraction &&
                  e.autoplay.run();
              },
              destroy: function destroy(e) {
                e.autoplay.detachMouseEvents(),
                  e.autoplay.running && e.autoplay.stop(),
                  getDocument().removeEventListener(
                    "visibilitychange",
                    e.autoplay.onVisibilityChange
                  );
              },
            },
          },
          {
            name: "effect-fade",
            params: {
              fadeEffect: {
                crossFade: !1,
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                fadeEffect: _extends$4({}, K),
              });
            },
            on: {
              beforeInit: function beforeInit(e) {
                if ("fade" === e.params.effect) {
                  e.classNames.push(e.params.containerModifierClass + "fade");
                  var t = {
                    slidesPerView: 1,
                    slidesPerColumn: 1,
                    slidesPerGroup: 1,
                    watchSlidesProgress: !0,
                    spaceBetween: 0,
                    virtualTranslate: !0,
                  };
                  extend(e.params, t), extend(e.originalParams, t);
                }
              },
              setTranslate: function setTranslate(e) {
                "fade" === e.params.effect && e.fadeEffect.setTranslate();
              },
              setTransition: function setTransition(e, t) {
                "fade" === e.params.effect && e.fadeEffect.setTransition(t);
              },
            },
          },
          {
            name: "effect-cube",
            params: {
              cubeEffect: {
                slideShadows: !0,
                shadow: !0,
                shadowOffset: 20,
                shadowScale: 0.94,
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                cubeEffect: _extends$3({}, ee),
              });
            },
            on: {
              beforeInit: function beforeInit(e) {
                if ("cube" === e.params.effect) {
                  e.classNames.push(e.params.containerModifierClass + "cube"),
                    e.classNames.push(e.params.containerModifierClass + "3d");
                  var t = {
                    slidesPerView: 1,
                    slidesPerColumn: 1,
                    slidesPerGroup: 1,
                    watchSlidesProgress: !0,
                    resistanceRatio: 0,
                    spaceBetween: 0,
                    centeredSlides: !1,
                    virtualTranslate: !0,
                  };
                  extend(e.params, t), extend(e.originalParams, t);
                }
              },
              setTranslate: function setTranslate(e) {
                "cube" === e.params.effect && e.cubeEffect.setTranslate();
              },
              setTransition: function setTransition(e, t) {
                "cube" === e.params.effect && e.cubeEffect.setTransition(t);
              },
            },
          },
          {
            name: "effect-flip",
            params: {
              flipEffect: {
                slideShadows: !0,
                limitRotation: !0,
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                flipEffect: _extends$2({}, te),
              });
            },
            on: {
              beforeInit: function beforeInit(e) {
                if ("flip" === e.params.effect) {
                  e.classNames.push(e.params.containerModifierClass + "flip"),
                    e.classNames.push(e.params.containerModifierClass + "3d");
                  var t = {
                    slidesPerView: 1,
                    slidesPerColumn: 1,
                    slidesPerGroup: 1,
                    watchSlidesProgress: !0,
                    spaceBetween: 0,
                    virtualTranslate: !0,
                  };
                  extend(e.params, t), extend(e.originalParams, t);
                }
              },
              setTranslate: function setTranslate(e) {
                "flip" === e.params.effect && e.flipEffect.setTranslate();
              },
              setTransition: function setTransition(e, t) {
                "flip" === e.params.effect && e.flipEffect.setTransition(t);
              },
            },
          },
          {
            name: "effect-coverflow",
            params: {
              coverflowEffect: {
                rotate: 50,
                stretch: 0,
                depth: 100,
                scale: 1,
                modifier: 1,
                slideShadows: !0,
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                coverflowEffect: _extends$1({}, ne),
              });
            },
            on: {
              beforeInit: function beforeInit(e) {
                "coverflow" === e.params.effect &&
                  (e.classNames.push(
                    e.params.containerModifierClass + "coverflow"
                  ),
                  e.classNames.push(e.params.containerModifierClass + "3d"),
                  (e.params.watchSlidesProgress = !0),
                  (e.originalParams.watchSlidesProgress = !0));
              },
              setTranslate: function setTranslate(e) {
                "coverflow" === e.params.effect &&
                  e.coverflowEffect.setTranslate();
              },
              setTransition: function setTransition(e, t) {
                "coverflow" === e.params.effect &&
                  e.coverflowEffect.setTransition(t);
              },
            },
          },
          {
            name: "thumbs",
            params: {
              thumbs: {
                swiper: null,
                multipleActiveThumbs: !0,
                autoScrollOffset: 0,
                slideThumbActiveClass: "swiper-slide-thumb-active",
                thumbsContainerClass: "swiper-container-thumbs",
              },
            },
            create: function create() {
              bindModuleMethods(this, {
                thumbs: _extends(
                  {
                    swiper: null,
                    initialized: !1,
                  },
                  ie
                ),
              });
            },
            on: {
              beforeInit: function beforeInit(e) {
                var t = e.params.thumbs;
                t && t.swiper && (e.thumbs.init(), e.thumbs.update(!0));
              },
              slideChange: function slideChange(e) {
                e.thumbs.swiper && e.thumbs.update();
              },
              update: function update(e) {
                e.thumbs.swiper && e.thumbs.update();
              },
              resize: function resize(e) {
                e.thumbs.swiper && e.thumbs.update();
              },
              observerUpdate: function observerUpdate(e) {
                e.thumbs.swiper && e.thumbs.update();
              },
              setTransition: function setTransition(e, t) {
                var n = e.thumbs.swiper;
                n && n.setTransition(t);
              },
              beforeDestroy: function beforeDestroy(e) {
                var t = e.thumbs.swiper;
                t && e.thumbs.swiperCreated && t && t.destroy();
              },
            },
          },
        ];
      O.use(re);
      var ae = 0,
        oe = (0, c.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, r.A)(this, _class),
                (e = (0, o.A)(this, _class)).__registerHost(),
                (e.onChange = (0, c.lh)(e, "change", 7)),
                (e.onAnimationFinish = (0, c.lh)(e, "animationfinish", 7)),
                (e._id = ae++),
                (e.handleSwiperLoopListen = function () {
                  var t, n, i;
                  (null === (t = e.observerFirst) || void 0 === t
                    ? void 0
                    : t.disconnect) && e.observerFirst.disconnect(),
                    (null === (n = e.observerLast) || void 0 === n
                      ? void 0
                      : n.disconnect) && e.observerLast.disconnect(),
                    (e.observerFirst = new MutationObserver(
                      e.handleSwiperLoopDebounce
                    )),
                    (e.observerLast = new MutationObserver(
                      e.handleSwiperLoopDebounce
                    ));
                  var r = (
                    null === (i = e.swiper.$wrapperEl) || void 0 === i
                      ? void 0
                      : i[0]
                  ).querySelectorAll(
                    "taro-swiper-item-core:not(.swiper-slide-duplicate)"
                  );
                  r.length >= 1
                    ? e.observerFirst.observe(r[0], {
                        characterData: !0,
                      })
                    : r.length >= 2 &&
                      e.observerLast.observe(r[r.length - 1], {
                        characterData: !0,
                      });
                }),
                (e.handleSwiperLoop = function () {
                  var t, n, i, r;
                  if (e.swiper && e.circular) {
                    var a = e.swiper;
                    (
                      (null === (t = e.swiperWrapper) || void 0 === t
                        ? void 0
                        : t.querySelectorAll(".swiper-slide-duplicate")) || []
                    ).length < 2
                      ? (null === (n = a.loopDestroy) ||
                          void 0 === n ||
                          n.call(a),
                        null === (i = a.loopCreate) ||
                          void 0 === i ||
                          i.call(a))
                      : null === (r = a.loopFix) || void 0 === r || r.call(a);
                  }
                }),
                (e.handleSwiperLoopDebounce = (0, u.d)(e.handleSwiperLoop, 50)),
                (e.handleSwiperSizeDebounce = (0, u.d)(function () {
                  e.swiper && !e.circular && e.swiper.updateSlides();
                }, 50)),
                (e.swiperWrapper = void 0),
                (e.swiper = void 0),
                (e.isWillLoadCalled = !1),
                (e.source = ""),
                (e.indicatorDots = !1),
                (e.indicatorColor = "rgba(0, 0, 0, .3)"),
                (e.indicatorActiveColor = "#000000"),
                (e.autoplay = !1),
                (e.current = 0),
                (e.interval = 5e3),
                (e.duration = 500),
                (e.circular = !1),
                (e.vertical = !1),
                (e.previousMargin = "0px"),
                (e.nextMargin = "0px"),
                (e.displayMultipleItems = 1),
                (e.full = !1),
                (e.zoom = !1),
                (e.observer = void 0),
                (e.observerFirst = void 0),
                (e.observerLast = void 0),
                e
              );
            }
            return (
              (0, s.A)(_class, e),
              (0, a.A)(
                _class,
                [
                  {
                    key: "watchCurrent",
                    value: function watchCurrent(e) {
                      if (this.isWillLoadCalled) {
                        var t = parseInt(e, 10);
                        isNaN(t) ||
                          (this.circular
                            ? this.swiper.isBeginning ||
                              this.swiper.isEnd ||
                              this.swiper.slideToLoop(t)
                            : this.swiper.slideTo(t));
                      }
                    },
                  },
                  {
                    key: "watchAutoplay",
                    value: function watchAutoplay(e) {
                      if (this.isWillLoadCalled && this.swiper) {
                        var t = this.swiper.autoplay;
                        if (t) {
                          if (t.running === e) return;
                          e
                            ? (this.swiper.params &&
                                "object" ===
                                  (0, l.A)(this.swiper.params.autoplay) &&
                                (!0 ===
                                  this.swiper.params.autoplay
                                    .disableOnInteraction &&
                                  (this.swiper.params.autoplay.disableOnInteraction =
                                    !1),
                                (this.swiper.params.autoplay.delay =
                                  this.interval)),
                              t.start())
                            : t.stop();
                        }
                      }
                    },
                  },
                  {
                    key: "watchDuration",
                    value: function watchDuration(e) {
                      this.isWillLoadCalled && (this.swiper.params.speed = e);
                    },
                  },
                  {
                    key: "watchInterval",
                    value: function watchInterval(e) {
                      this.isWillLoadCalled &&
                        "object" === (0, l.A)(this.swiper.params.autoplay) &&
                        (this.swiper.params.autoplay.delay = e);
                    },
                  },
                  {
                    key: "watchSwiperWrapper",
                    value: function watchSwiperWrapper(e) {
                      this.isWillLoadCalled &&
                        e &&
                        ((this.el.appendChild = function (t) {
                          return e.appendChild(t);
                        }),
                        (this.el.insertBefore = function (t, n) {
                          return e.insertBefore(t, n);
                        }),
                        (this.el.replaceChild = function (t, n) {
                          return e.replaceChild(t, n);
                        }),
                        (this.el.removeChild = function (t) {
                          return e.removeChild(t);
                        }),
                        this.el.addEventListener(
                          "DOMNodeInserted",
                          this.handleSwiperSizeDebounce
                        ),
                        this.el.addEventListener(
                          "DOMNodeRemoved",
                          this.handleSwiperSizeDebounce
                        ),
                        this.el.addEventListener(
                          "MutationObserver",
                          this.handleSwiperSizeDebounce
                        ));
                    },
                  },
                  {
                    key: "watchCircular",
                    value: function watchCircular() {
                      this.swiper && (this.swiper.destroy(), this.handleInit());
                    },
                  },
                  {
                    key: "watchDisplayMultipleItems",
                    value: function watchDisplayMultipleItems() {
                      this.swiper && (this.swiper.destroy(), this.handleInit());
                    },
                  },
                  {
                    key: "componentWillLoad",
                    value: function componentWillLoad() {
                      this.isWillLoadCalled = !0;
                    },
                  },
                  {
                    key: "componentDidLoad",
                    value: function componentDidLoad() {
                      var e;
                      if ((this.handleInit(), this.swiper && this.circular)) {
                        var t =
                          null === (e = this.swiper.$wrapperEl) || void 0 === e
                            ? void 0
                            : e[0];
                        (this.observer = new MutationObserver(
                          this.handleSwiperLoopListen
                        )),
                          this.observer.observe(t, {
                            childList: !0,
                          });
                      }
                    },
                  },
                  {
                    key: "componentWillUpdate",
                    value: function componentWillUpdate() {
                      var e, t;
                      this.swiper &&
                        (this.autoplay &&
                          !(null === (e = this.swiper.autoplay) || void 0 === e
                            ? void 0
                            : e.running) &&
                          (null === (t = this.swiper.autoplay) ||
                            void 0 === t ||
                            t.start()),
                        this.swiper.update());
                    },
                  },
                  {
                    key: "componentDidRender",
                    value: function componentDidRender() {
                      this.handleSwiperLoop();
                    },
                  },
                  {
                    key: "disconnectedCallback",
                    value: function disconnectedCallback() {
                      var e, t, n, i, r, a;
                      this.el.removeEventListener(
                        "DOMNodeInserted",
                        this.handleSwiperSizeDebounce
                      ),
                        this.el.removeEventListener(
                          "DOMNodeRemoved",
                          this.handleSwiperSizeDebounce
                        ),
                        this.el.removeEventListener(
                          "MutationObserver",
                          this.handleSwiperSizeDebounce
                        ),
                        null ===
                          (t =
                            null === (e = this.observer) || void 0 === e
                              ? void 0
                              : e.disconnect) ||
                          void 0 === t ||
                          t.call(e),
                        null ===
                          (i =
                            null === (n = this.observerFirst) || void 0 === n
                              ? void 0
                              : n.disconnect) ||
                          void 0 === i ||
                          i.call(n),
                        null ===
                          (a =
                            null === (r = this.observerLast) || void 0 === r
                              ? void 0
                              : r.disconnect) ||
                          void 0 === a ||
                          a.call(r);
                    },
                  },
                  {
                    key: "handleInit",
                    value: function handleInit() {
                      var e = this.autoplay,
                        t = this.circular,
                        n = this.current,
                        i = this.displayMultipleItems,
                        r = this.duration,
                        a = this.interval,
                        o = this.vertical,
                        s = this,
                        l = {
                          pagination: {
                            el: ".taro-swiper-".concat(
                              this._id,
                              " > .swiper-container > .swiper-pagination"
                            ),
                          },
                          direction: o ? "vertical" : "horizontal",
                          loop: t,
                          slidesPerView: i,
                          initialSlide: t ? n + 1 : n,
                          speed: r,
                          observer: !0,
                          observeParents: !0,
                          zoom: this.zoom,
                          on: {
                            slideTo: function slideTo() {
                              s.current = this.realIndex;
                            },
                            slideChangeTransitionEnd:
                              function slideChangeTransitionEnd() {
                                t && (this.isBeginning || this.isEnd)
                                  ? this.slideToLoop(this.realIndex, 0)
                                  : s.onChange.emit({
                                      current: this.realIndex,
                                      source: s.source,
                                    });
                              },
                            touchEnd: function touchEnd() {
                              s.source = "touch";
                            },
                            autoplay: function autoplay() {
                              s.source = "autoplay";
                            },
                            transitionEnd: function transitionEnd() {
                              setTimeout(function () {
                                s.source = "";
                              }),
                                s.onAnimationFinish.emit({
                                  current: this.realIndex,
                                  source: "",
                                });
                            },
                            observerUpdate: function observerUpdate(e, t) {
                              var n = t.target;
                              (n && "string" == typeof n.className
                                ? n.className
                                : ""
                              ).includes("taro_page") &&
                                "none" !== n.style.display &&
                                s.autoplay &&
                                n.contains(e.$el[0]) &&
                                (s.circular
                                  ? e.slideToLoop(this.realIndex, 0)
                                  : e.slideTo(this.realIndex));
                            },
                          },
                        };
                      e &&
                        (l.autoplay = {
                          delay: a,
                          disableOnInteraction: !1,
                        }),
                        (this.swiper = new O(
                          ".taro-swiper-".concat(
                            this._id,
                            " > .swiper-container"
                          ),
                          l
                        )),
                        (this.swiperWrapper = this.el.querySelector(
                          ".taro-swiper-".concat(
                            this._id,
                            " > .swiper-container > .swiper-wrapper"
                          )
                        ));
                    },
                  },
                  {
                    key: "render",
                    value: function render() {
                      var e = this.vertical,
                        t = this.indicatorDots,
                        n = this.indicatorColor,
                        r = this.indicatorActiveColor,
                        a = {
                          overflow: "hidden",
                        },
                        o = {
                          overflow: "visible",
                        };
                      this.full && ((a.height = "100%"), (o.height = "100%"));
                      var s = /^(\d+)px/.exec(this.previousMargin) || [],
                        l = (0, i.A)(s, 2)[1],
                        u = /^(\d+)px/.exec(this.nextMargin) || [],
                        p = (0, i.A)(u, 2)[1],
                        h = parseInt(l) || 0,
                        m = parseInt(p) || 0;
                      return (
                        e
                          ? ((o.marginTop = "".concat(h, "px")),
                            (o.marginBottom = "".concat(m, "px")))
                          : ((o.marginRight = "".concat(m, "px")),
                            (o.marginLeft = "".concat(h, "px"))),
                        (0, c.h)(
                          c.xr,
                          {
                            class: "taro-swiper-".concat(this._id),
                            style: a,
                          },
                          (0, c.h)(
                            "div",
                            {
                              class: "swiper-container",
                              style: o,
                            },
                            (0, c.h)(
                              "style",
                              {
                                type: "text/css",
                              },
                              "\n              .taro-swiper-"
                                .concat(
                                  this._id,
                                  " > .swiper-container > .swiper-pagination > .swiper-pagination-bullet { background: "
                                )
                                .concat(n, " }\n              .taro-swiper-")
                                .concat(
                                  this._id,
                                  " > .swiper-container > .swiper-pagination > .swiper-pagination-bullet-active { background: "
                                )
                                .concat(r, " }\n            ")
                            ),
                            (0, c.h)(
                              "div",
                              {
                                class: "swiper-wrapper",
                              },
                              (0, c.h)("slot", null)
                            ),
                            (0, c.h)("div", {
                              class: (0, d.c)("swiper-pagination", {
                                "swiper-pagination-hidden": !t,
                                "swiper-pagination-bullets": t,
                              }),
                            })
                          )
                        )
                      );
                    },
                  },
                  {
                    key: "el",
                    get: function get() {
                      return this;
                    },
                  },
                ],
                [
                  {
                    key: "watchers",
                    get: function get() {
                      return {
                        current: ["watchCurrent"],
                        autoplay: ["watchAutoplay"],
                        duration: ["watchDuration"],
                        interval: ["watchInterval"],
                        swiperWrapper: ["watchSwiperWrapper"],
                        circular: ["watchCircular"],
                        displayMultipleItems: ["watchDisplayMultipleItems"],
                      };
                    },
                  },
                  {
                    key: "style",
                    get: function get() {
                      return "@font-face{font-family:swiper-icons;src:url('data:application/font-woff;charset=utf-8;base64, d09GRgABAAAAAAZgABAAAAAADAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABGRlRNAAAGRAAAABoAAAAci6qHkUdERUYAAAWgAAAAIwAAACQAYABXR1BPUwAABhQAAAAuAAAANuAY7+xHU1VCAAAFxAAAAFAAAABm2fPczU9TLzIAAAHcAAAASgAAAGBP9V5RY21hcAAAAkQAAACIAAABYt6F0cBjdnQgAAACzAAAAAQAAAAEABEBRGdhc3AAAAWYAAAACAAAAAj//wADZ2x5ZgAAAywAAADMAAAD2MHtryVoZWFkAAABbAAAADAAAAA2E2+eoWhoZWEAAAGcAAAAHwAAACQC9gDzaG10eAAAAigAAAAZAAAArgJkABFsb2NhAAAC0AAAAFoAAABaFQAUGG1heHAAAAG8AAAAHwAAACAAcABAbmFtZQAAA/gAAAE5AAACXvFdBwlwb3N0AAAFNAAAAGIAAACE5s74hXjaY2BkYGAAYpf5Hu/j+W2+MnAzMYDAzaX6QjD6/4//Bxj5GA8AuRwMYGkAPywL13jaY2BkYGA88P8Agx4j+/8fQDYfA1AEBWgDAIB2BOoAeNpjYGRgYNBh4GdgYgABEMnIABJzYNADCQAACWgAsQB42mNgYfzCOIGBlYGB0YcxjYGBwR1Kf2WQZGhhYGBiYGVmgAFGBiQQkOaawtDAoMBQxXjg/wEGPcYDDA4wNUA2CCgwsAAAO4EL6gAAeNpj2M0gyAACqxgGNWBkZ2D4/wMA+xkDdgAAAHjaY2BgYGaAYBkGRgYQiAHyGMF8FgYHIM3DwMHABGQrMOgyWDLEM1T9/w8UBfEMgLzE////P/5//f/V/xv+r4eaAAeMbAxwIUYmIMHEgKYAYjUcsDAwsLKxc3BycfPw8jEQA/gZBASFhEVExcQlJKWkZWTl5BUUlZRVVNXUNTQZBgMAAMR+E+gAEQFEAAAAKgAqACoANAA+AEgAUgBcAGYAcAB6AIQAjgCYAKIArAC2AMAAygDUAN4A6ADyAPwBBgEQARoBJAEuATgBQgFMAVYBYAFqAXQBfgGIAZIBnAGmAbIBzgHsAAB42u2NMQ6CUAyGW568x9AneYYgm4MJbhKFaExIOAVX8ApewSt4Bic4AfeAid3VOBixDxfPYEza5O+Xfi04YADggiUIULCuEJK8VhO4bSvpdnktHI5QCYtdi2sl8ZnXaHlqUrNKzdKcT8cjlq+rwZSvIVczNiezsfnP/uznmfPFBNODM2K7MTQ45YEAZqGP81AmGGcF3iPqOop0r1SPTaTbVkfUe4HXj97wYE+yNwWYxwWu4v1ugWHgo3S1XdZEVqWM7ET0cfnLGxWfkgR42o2PvWrDMBSFj/IHLaF0zKjRgdiVMwScNRAoWUoH78Y2icB/yIY09An6AH2Bdu/UB+yxopYshQiEvnvu0dURgDt8QeC8PDw7Fpji3fEA4z/PEJ6YOB5hKh4dj3EvXhxPqH/SKUY3rJ7srZ4FZnh1PMAtPhwP6fl2PMJMPDgeQ4rY8YT6Gzao0eAEA409DuggmTnFnOcSCiEiLMgxCiTI6Cq5DZUd3Qmp10vO0LaLTd2cjN4fOumlc7lUYbSQcZFkutRG7g6JKZKy0RmdLY680CDnEJ+UMkpFFe1RN7nxdVpXrC4aTtnaurOnYercZg2YVmLN/d/gczfEimrE/fs/bOuq29Zmn8tloORaXgZgGa78yO9/cnXm2BpaGvq25Dv9S4E9+5SIc9PqupJKhYFSSl47+Qcr1mYNAAAAeNptw0cKwkAAAMDZJA8Q7OUJvkLsPfZ6zFVERPy8qHh2YER+3i/BP83vIBLLySsoKimrqKqpa2hp6+jq6RsYGhmbmJqZSy0sraxtbO3sHRydnEMU4uR6yx7JJXveP7WrDycAAAAAAAH//wACeNpjYGRgYOABYhkgZgJCZgZNBkYGLQZtIJsFLMYAAAw3ALgAeNolizEKgDAQBCchRbC2sFER0YD6qVQiBCv/H9ezGI6Z5XBAw8CBK/m5iQQVauVbXLnOrMZv2oLdKFa8Pjuru2hJzGabmOSLzNMzvutpB3N42mNgZGBg4GKQYzBhYMxJLMlj4GBgAYow/P/PAJJhLM6sSoWKfWCAAwDAjgbRAAB42mNgYGBkAIIbCZo5IPrmUn0hGA0AO8EFTQAA') format('woff');font-weight:400;font-style:normal}:root{--swiper-theme-color:#007aff}.swiper-container{margin-left:auto;margin-right:auto;position:relative;overflow:hidden;list-style:none;padding:0;z-index:1}.swiper-container-vertical>.swiper-wrapper{-ms-flex-direction:column;flex-direction:column}.swiper-wrapper{position:relative;width:100%;height:100%;z-index:1;display:-ms-flexbox;display:flex;-webkit-transition-property:-webkit-transform;transition-property:-webkit-transform;transition-property:transform;transition-property:transform, -webkit-transform;-webkit-box-sizing:content-box;box-sizing:content-box}.swiper-container-android .swiper-slide,.swiper-wrapper{-webkit-transform:translate3d(0px,0,0);transform:translate3d(0px,0,0)}.swiper-container-multirow>.swiper-wrapper{-ms-flex-wrap:wrap;flex-wrap:wrap}.swiper-container-multirow-column>.swiper-wrapper{-ms-flex-wrap:wrap;flex-wrap:wrap;-ms-flex-direction:column;flex-direction:column}.swiper-container-free-mode>.swiper-wrapper{-webkit-transition-timing-function:ease-out;transition-timing-function:ease-out;margin:0 auto}.swiper-container-pointer-events{-ms-touch-action:pan-y;touch-action:pan-y}.swiper-container-pointer-events.swiper-container-vertical{-ms-touch-action:pan-x;touch-action:pan-x}.swiper-slide{-ms-flex-negative:0;flex-shrink:0;width:100%;height:100%;position:relative;-webkit-transition-property:-webkit-transform;transition-property:-webkit-transform;transition-property:transform;transition-property:transform, -webkit-transform}.swiper-slide-invisible-blank{visibility:hidden}.swiper-container-autoheight,.swiper-container-autoheight .swiper-slide{height:auto}.swiper-container-autoheight .swiper-wrapper{-ms-flex-align:start;align-items:flex-start;-webkit-transition-property:height,-webkit-transform;transition-property:height,-webkit-transform;transition-property:transform,height;transition-property:transform,height,-webkit-transform}.swiper-container-3d{-webkit-perspective:1200px;perspective:1200px}.swiper-container-3d .swiper-cube-shadow,.swiper-container-3d .swiper-slide,.swiper-container-3d .swiper-slide-shadow-bottom,.swiper-container-3d .swiper-slide-shadow-left,.swiper-container-3d .swiper-slide-shadow-right,.swiper-container-3d .swiper-slide-shadow-top,.swiper-container-3d .swiper-wrapper{-webkit-transform-style:preserve-3d;transform-style:preserve-3d}.swiper-container-3d .swiper-slide-shadow-bottom,.swiper-container-3d .swiper-slide-shadow-left,.swiper-container-3d .swiper-slide-shadow-right,.swiper-container-3d .swiper-slide-shadow-top{position:absolute;left:0;top:0;width:100%;height:100%;pointer-events:none;z-index:10}.swiper-container-3d .swiper-slide-shadow-left{background-image:-webkit-gradient(linear,right top, left top,from(rgba(0,0,0,.5)),to(rgba(0,0,0,0)));background-image:linear-gradient(to left,rgba(0,0,0,.5),rgba(0,0,0,0))}.swiper-container-3d .swiper-slide-shadow-right{background-image:-webkit-gradient(linear,left top, right top,from(rgba(0,0,0,.5)),to(rgba(0,0,0,0)));background-image:linear-gradient(to right,rgba(0,0,0,.5),rgba(0,0,0,0))}.swiper-container-3d .swiper-slide-shadow-top{background-image:-webkit-gradient(linear,left bottom, left top,from(rgba(0,0,0,.5)),to(rgba(0,0,0,0)));background-image:linear-gradient(to top,rgba(0,0,0,.5),rgba(0,0,0,0))}.swiper-container-3d .swiper-slide-shadow-bottom{background-image:-webkit-gradient(linear,left top, left bottom,from(rgba(0,0,0,.5)),to(rgba(0,0,0,0)));background-image:linear-gradient(to bottom,rgba(0,0,0,.5),rgba(0,0,0,0))}.swiper-container-css-mode>.swiper-wrapper{overflow:auto;scrollbar-width:none;-ms-overflow-style:none}.swiper-container-css-mode>.swiper-wrapper::-webkit-scrollbar{display:none}.swiper-container-css-mode>.swiper-wrapper>.swiper-slide{scroll-snap-align:start start}.swiper-container-horizontal.swiper-container-css-mode>.swiper-wrapper{-webkit-scroll-snap-type:x mandatory;-ms-scroll-snap-type:x mandatory;scroll-snap-type:x mandatory}.swiper-container-vertical.swiper-container-css-mode>.swiper-wrapper{-webkit-scroll-snap-type:y mandatory;-ms-scroll-snap-type:y mandatory;scroll-snap-type:y mandatory}@font-face{font-family:swiper-icons;src:url('data:application/font-woff;charset=utf-8;base64, d09GRgABAAAAAAZgABAAAAAADAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABGRlRNAAAGRAAAABoAAAAci6qHkUdERUYAAAWgAAAAIwAAACQAYABXR1BPUwAABhQAAAAuAAAANuAY7+xHU1VCAAAFxAAAAFAAAABm2fPczU9TLzIAAAHcAAAASgAAAGBP9V5RY21hcAAAAkQAAACIAAABYt6F0cBjdnQgAAACzAAAAAQAAAAEABEBRGdhc3AAAAWYAAAACAAAAAj//wADZ2x5ZgAAAywAAADMAAAD2MHtryVoZWFkAAABbAAAADAAAAA2E2+eoWhoZWEAAAGcAAAAHwAAACQC9gDzaG10eAAAAigAAAAZAAAArgJkABFsb2NhAAAC0AAAAFoAAABaFQAUGG1heHAAAAG8AAAAHwAAACAAcABAbmFtZQAAA/gAAAE5AAACXvFdBwlwb3N0AAAFNAAAAGIAAACE5s74hXjaY2BkYGAAYpf5Hu/j+W2+MnAzMYDAzaX6QjD6/4//Bxj5GA8AuRwMYGkAPywL13jaY2BkYGA88P8Agx4j+/8fQDYfA1AEBWgDAIB2BOoAeNpjYGRgYNBh4GdgYgABEMnIABJzYNADCQAACWgAsQB42mNgYfzCOIGBlYGB0YcxjYGBwR1Kf2WQZGhhYGBiYGVmgAFGBiQQkOaawtDAoMBQxXjg/wEGPcYDDA4wNUA2CCgwsAAAO4EL6gAAeNpj2M0gyAACqxgGNWBkZ2D4/wMA+xkDdgAAAHjaY2BgYGaAYBkGRgYQiAHyGMF8FgYHIM3DwMHABGQrMOgyWDLEM1T9/w8UBfEMgLzE////P/5//f/V/xv+r4eaAAeMbAxwIUYmIMHEgKYAYjUcsDAwsLKxc3BycfPw8jEQA/gZBASFhEVExcQlJKWkZWTl5BUUlZRVVNXUNTQZBgMAAMR+E+gAEQFEAAAAKgAqACoANAA+AEgAUgBcAGYAcAB6AIQAjgCYAKIArAC2AMAAygDUAN4A6ADyAPwBBgEQARoBJAEuATgBQgFMAVYBYAFqAXQBfgGIAZIBnAGmAbIBzgHsAAB42u2NMQ6CUAyGW568x9AneYYgm4MJbhKFaExIOAVX8ApewSt4Bic4AfeAid3VOBixDxfPYEza5O+Xfi04YADggiUIULCuEJK8VhO4bSvpdnktHI5QCYtdi2sl8ZnXaHlqUrNKzdKcT8cjlq+rwZSvIVczNiezsfnP/uznmfPFBNODM2K7MTQ45YEAZqGP81AmGGcF3iPqOop0r1SPTaTbVkfUe4HXj97wYE+yNwWYxwWu4v1ugWHgo3S1XdZEVqWM7ET0cfnLGxWfkgR42o2PvWrDMBSFj/IHLaF0zKjRgdiVMwScNRAoWUoH78Y2icB/yIY09An6AH2Bdu/UB+yxopYshQiEvnvu0dURgDt8QeC8PDw7Fpji3fEA4z/PEJ6YOB5hKh4dj3EvXhxPqH/SKUY3rJ7srZ4FZnh1PMAtPhwP6fl2PMJMPDgeQ4rY8YT6Gzao0eAEA409DuggmTnFnOcSCiEiLMgxCiTI6Cq5DZUd3Qmp10vO0LaLTd2cjN4fOumlc7lUYbSQcZFkutRG7g6JKZKy0RmdLY680CDnEJ+UMkpFFe1RN7nxdVpXrC4aTtnaurOnYercZg2YVmLN/d/gczfEimrE/fs/bOuq29Zmn8tloORaXgZgGa78yO9/cnXm2BpaGvq25Dv9S4E9+5SIc9PqupJKhYFSSl47+Qcr1mYNAAAAeNptw0cKwkAAAMDZJA8Q7OUJvkLsPfZ6zFVERPy8qHh2YER+3i/BP83vIBLLySsoKimrqKqpa2hp6+jq6RsYGhmbmJqZSy0sraxtbO3sHRydnEMU4uR6yx7JJXveP7WrDycAAAAAAAH//wACeNpjYGRgYOABYhkgZgJCZgZNBkYGLQZtIJsFLMYAAAw3ALgAeNolizEKgDAQBCchRbC2sFER0YD6qVQiBCv/H9ezGI6Z5XBAw8CBK/m5iQQVauVbXLnOrMZv2oLdKFa8Pjuru2hJzGabmOSLzNMzvutpB3N42mNgZGBg4GKQYzBhYMxJLMlj4GBgAYow/P/PAJJhLM6sSoWKfWCAAwDAjgbRAAB42mNgYGBkAIIbCZo5IPrmUn0hGA0AO8EFTQAA') format('woff');font-weight:400;font-style:normal}:root{--swiper-theme-color:#007aff}.swiper-container{margin-left:auto;margin-right:auto;position:relative;overflow:hidden;list-style:none;padding:0;z-index:1}.swiper-container-vertical>.swiper-wrapper{-ms-flex-direction:column;flex-direction:column}.swiper-wrapper{position:relative;width:100%;height:100%;z-index:1;display:-ms-flexbox;display:flex;-webkit-transition-property:-webkit-transform;transition-property:-webkit-transform;transition-property:transform;transition-property:transform, -webkit-transform;-webkit-box-sizing:content-box;box-sizing:content-box}.swiper-container-android .swiper-slide,.swiper-wrapper{-webkit-transform:translate3d(0px,0,0);transform:translate3d(0px,0,0)}.swiper-container-multirow>.swiper-wrapper{-ms-flex-wrap:wrap;flex-wrap:wrap}.swiper-container-multirow-column>.swiper-wrapper{-ms-flex-wrap:wrap;flex-wrap:wrap;-ms-flex-direction:column;flex-direction:column}.swiper-container-free-mode>.swiper-wrapper{-webkit-transition-timing-function:ease-out;transition-timing-function:ease-out;margin:0 auto}.swiper-container-pointer-events{-ms-touch-action:pan-y;touch-action:pan-y}.swiper-container-pointer-events.swiper-container-vertical{-ms-touch-action:pan-x;touch-action:pan-x}.swiper-slide{-ms-flex-negative:0;flex-shrink:0;width:100%;height:100%;position:relative;-webkit-transition-property:-webkit-transform;transition-property:-webkit-transform;transition-property:transform;transition-property:transform, -webkit-transform}.swiper-slide-invisible-blank{visibility:hidden}.swiper-container-autoheight,.swiper-container-autoheight .swiper-slide{height:auto}.swiper-container-autoheight .swiper-wrapper{-ms-flex-align:start;align-items:flex-start;-webkit-transition-property:height,-webkit-transform;transition-property:height,-webkit-transform;transition-property:transform,height;transition-property:transform,height,-webkit-transform}.swiper-container-3d{-webkit-perspective:1200px;perspective:1200px}.swiper-container-3d .swiper-cube-shadow,.swiper-container-3d .swiper-slide,.swiper-container-3d .swiper-slide-shadow-bottom,.swiper-container-3d .swiper-slide-shadow-left,.swiper-container-3d .swiper-slide-shadow-right,.swiper-container-3d .swiper-slide-shadow-top,.swiper-container-3d .swiper-wrapper{-webkit-transform-style:preserve-3d;transform-style:preserve-3d}.swiper-container-3d .swiper-slide-shadow-bottom,.swiper-container-3d .swiper-slide-shadow-left,.swiper-container-3d .swiper-slide-shadow-right,.swiper-container-3d .swiper-slide-shadow-top{position:absolute;left:0;top:0;width:100%;height:100%;pointer-events:none;z-index:10}.swiper-container-3d .swiper-slide-shadow-left{background-image:-webkit-gradient(linear,right top, left top,from(rgba(0,0,0,.5)),to(rgba(0,0,0,0)));background-image:linear-gradient(to left,rgba(0,0,0,.5),rgba(0,0,0,0))}.swiper-container-3d .swiper-slide-shadow-right{background-image:-webkit-gradient(linear,left top, right top,from(rgba(0,0,0,.5)),to(rgba(0,0,0,0)));background-image:linear-gradient(to right,rgba(0,0,0,.5),rgba(0,0,0,0))}.swiper-container-3d .swiper-slide-shadow-top{background-image:-webkit-gradient(linear,left bottom, left top,from(rgba(0,0,0,.5)),to(rgba(0,0,0,0)));background-image:linear-gradient(to top,rgba(0,0,0,.5),rgba(0,0,0,0))}.swiper-container-3d .swiper-slide-shadow-bottom{background-image:-webkit-gradient(linear,left top, left bottom,from(rgba(0,0,0,.5)),to(rgba(0,0,0,0)));background-image:linear-gradient(to bottom,rgba(0,0,0,.5),rgba(0,0,0,0))}.swiper-container-css-mode>.swiper-wrapper{overflow:auto;scrollbar-width:none;-ms-overflow-style:none}.swiper-container-css-mode>.swiper-wrapper::-webkit-scrollbar{display:none}.swiper-container-css-mode>.swiper-wrapper>.swiper-slide{scroll-snap-align:start start}.swiper-container-horizontal.swiper-container-css-mode>.swiper-wrapper{-webkit-scroll-snap-type:x mandatory;-ms-scroll-snap-type:x mandatory;scroll-snap-type:x mandatory}.swiper-container-vertical.swiper-container-css-mode>.swiper-wrapper{-webkit-scroll-snap-type:y mandatory;-ms-scroll-snap-type:y mandatory;scroll-snap-type:y mandatory}:root{--swiper-navigation-size:44px}.swiper-button-next,.swiper-button-prev{position:absolute;top:50%;width:calc(var(--swiper-navigation-size)/ 44 * 27);height:var(--swiper-navigation-size);margin-top:calc(0px - (var(--swiper-navigation-size)/ 2));z-index:10;cursor:pointer;display:-ms-flexbox;display:flex;-ms-flex-align:center;align-items:center;-ms-flex-pack:center;justify-content:center;color:var(--swiper-navigation-color,var(--swiper-theme-color))}.swiper-button-next.swiper-button-disabled,.swiper-button-prev.swiper-button-disabled{opacity:.35;cursor:auto;pointer-events:none}.swiper-button-next:after,.swiper-button-prev:after{font-family:swiper-icons;font-size:var(--swiper-navigation-size);text-transform:none!important;letter-spacing:0;text-transform:none;font-variant:initial;line-height:1}.swiper-button-prev,.swiper-container-rtl .swiper-button-next{left:10px;right:auto}.swiper-button-prev:after,.swiper-container-rtl .swiper-button-next:after{content:'prev'}.swiper-button-next,.swiper-container-rtl .swiper-button-prev{right:10px;left:auto}.swiper-button-next:after,.swiper-container-rtl .swiper-button-prev:after{content:'next'}.swiper-button-next.swiper-button-white,.swiper-button-prev.swiper-button-white{--swiper-navigation-color:#ffffff}.swiper-button-next.swiper-button-black,.swiper-button-prev.swiper-button-black{--swiper-navigation-color:#000000}.swiper-button-lock{display:none}.swiper-pagination{position:absolute;text-align:center;-webkit-transition:.3s opacity;transition:.3s opacity;-webkit-transform:translate3d(0,0,0);transform:translate3d(0,0,0);z-index:10}.swiper-pagination.swiper-pagination-hidden{opacity:0}.swiper-container-horizontal>.swiper-pagination-bullets,.swiper-pagination-custom,.swiper-pagination-fraction{bottom:10px;left:0;width:100%}.swiper-pagination-bullets-dynamic{overflow:hidden;font-size:0}.swiper-pagination-bullets-dynamic .swiper-pagination-bullet{-webkit-transform:scale(.33);transform:scale(.33);position:relative}.swiper-pagination-bullets-dynamic .swiper-pagination-bullet-active{-webkit-transform:scale(1);transform:scale(1)}.swiper-pagination-bullets-dynamic .swiper-pagination-bullet-active-main{-webkit-transform:scale(1);transform:scale(1)}.swiper-pagination-bullets-dynamic .swiper-pagination-bullet-active-prev{-webkit-transform:scale(.66);transform:scale(.66)}.swiper-pagination-bullets-dynamic .swiper-pagination-bullet-active-prev-prev{-webkit-transform:scale(.33);transform:scale(.33)}.swiper-pagination-bullets-dynamic .swiper-pagination-bullet-active-next{-webkit-transform:scale(.66);transform:scale(.66)}.swiper-pagination-bullets-dynamic .swiper-pagination-bullet-active-next-next{-webkit-transform:scale(.33);transform:scale(.33)}.swiper-pagination-bullet{width:8px;height:8px;display:inline-block;border-radius:50%;background:#000;opacity:.2}button.swiper-pagination-bullet{border:none;margin:0;padding:0;-webkit-box-shadow:none;box-shadow:none;-webkit-appearance:none;-moz-appearance:none;appearance:none}.swiper-pagination-clickable .swiper-pagination-bullet{cursor:pointer}.swiper-pagination-bullet-active{opacity:1;background:var(--swiper-pagination-color,var(--swiper-theme-color))}.swiper-container-vertical>.swiper-pagination-bullets{right:10px;top:50%;-webkit-transform:translate3d(0px,-50%,0);transform:translate3d(0px,-50%,0)}.swiper-container-vertical>.swiper-pagination-bullets .swiper-pagination-bullet{margin:6px 0;display:block}.swiper-container-vertical>.swiper-pagination-bullets.swiper-pagination-bullets-dynamic{top:50%;-webkit-transform:translateY(-50%);transform:translateY(-50%);width:8px}.swiper-container-vertical>.swiper-pagination-bullets.swiper-pagination-bullets-dynamic .swiper-pagination-bullet{display:inline-block;-webkit-transition:.2s transform,.2s top;transition:.2s transform,.2s top}.swiper-container-horizontal>.swiper-pagination-bullets .swiper-pagination-bullet{margin:0 4px}.swiper-container-horizontal>.swiper-pagination-bullets.swiper-pagination-bullets-dynamic{left:50%;-webkit-transform:translateX(-50%);transform:translateX(-50%);white-space:nowrap}.swiper-container-horizontal>.swiper-pagination-bullets.swiper-pagination-bullets-dynamic .swiper-pagination-bullet{-webkit-transition:.2s transform,.2s left;transition:.2s transform,.2s left}.swiper-container-horizontal.swiper-container-rtl>.swiper-pagination-bullets-dynamic .swiper-pagination-bullet{-webkit-transition:.2s transform,.2s right;transition:.2s transform,.2s right}.swiper-pagination-progressbar{background:rgba(0,0,0,.25);position:absolute}.swiper-pagination-progressbar .swiper-pagination-progressbar-fill{background:var(--swiper-pagination-color,var(--swiper-theme-color));position:absolute;left:0;top:0;width:100%;height:100%;-webkit-transform:scale(0);transform:scale(0);-webkit-transform-origin:left top;transform-origin:left top}.swiper-container-rtl .swiper-pagination-progressbar .swiper-pagination-progressbar-fill{-webkit-transform-origin:right top;transform-origin:right top}.swiper-container-horizontal>.swiper-pagination-progressbar,.swiper-container-vertical>.swiper-pagination-progressbar.swiper-pagination-progressbar-opposite{width:100%;height:4px;left:0;top:0}.swiper-container-horizontal>.swiper-pagination-progressbar.swiper-pagination-progressbar-opposite,.swiper-container-vertical>.swiper-pagination-progressbar{width:4px;height:100%;left:0;top:0}.swiper-pagination-white{--swiper-pagination-color:#ffffff}.swiper-pagination-black{--swiper-pagination-color:#000000}.swiper-pagination-lock{display:none}.swiper-scrollbar{border-radius:10px;position:relative;-ms-touch-action:none;background:rgba(0,0,0,.1)}.swiper-container-horizontal>.swiper-scrollbar{position:absolute;left:1%;bottom:3px;z-index:50;height:5px;width:98%}.swiper-container-vertical>.swiper-scrollbar{position:absolute;right:3px;top:1%;z-index:50;width:5px;height:98%}.swiper-scrollbar-drag{height:100%;width:100%;position:relative;background:rgba(0,0,0,.5);border-radius:10px;left:0;top:0}.swiper-scrollbar-cursor-drag{cursor:move}.swiper-scrollbar-lock{display:none}.swiper-zoom-container{width:100%;height:100%;display:-ms-flexbox;display:flex;-ms-flex-pack:center;justify-content:center;-ms-flex-align:center;align-items:center;text-align:center}.swiper-zoom-container>canvas,.swiper-zoom-container>img,.swiper-zoom-container>svg{max-width:100%;max-height:100%;-o-object-fit:contain;object-fit:contain}.swiper-slide-zoomed{cursor:move}.swiper-lazy-preloader{width:42px;height:42px;position:absolute;left:50%;top:50%;margin-left:-21px;margin-top:-21px;z-index:10;-webkit-transform-origin:50%;transform-origin:50%;-webkit-animation:swiper-preloader-spin 1s infinite linear;animation:swiper-preloader-spin 1s infinite linear;-webkit-box-sizing:border-box;box-sizing:border-box;border:4px solid var(--swiper-preloader-color,var(--swiper-theme-color));border-radius:50%;border-top-color:transparent}.swiper-lazy-preloader-white{--swiper-preloader-color:#fff}.swiper-lazy-preloader-black{--swiper-preloader-color:#000}@-webkit-keyframes swiper-preloader-spin{100%{-webkit-transform:rotate(360deg);transform:rotate(360deg)}}@keyframes swiper-preloader-spin{100%{-webkit-transform:rotate(360deg);transform:rotate(360deg)}}.swiper-container .swiper-notification{position:absolute;left:0;top:0;pointer-events:none;opacity:0;z-index:-1000}.swiper-container-fade.swiper-container-free-mode .swiper-slide{-webkit-transition-timing-function:ease-out;transition-timing-function:ease-out}.swiper-container-fade .swiper-slide{pointer-events:none;-webkit-transition-property:opacity;transition-property:opacity}.swiper-container-fade .swiper-slide .swiper-slide{pointer-events:none}.swiper-container-fade .swiper-slide-active,.swiper-container-fade .swiper-slide-active .swiper-slide-active{pointer-events:auto}.swiper-container-cube{overflow:visible}.swiper-container-cube .swiper-slide{pointer-events:none;-webkit-backface-visibility:hidden;backface-visibility:hidden;z-index:1;visibility:hidden;-webkit-transform-origin:0 0;transform-origin:0 0;width:100%;height:100%}.swiper-container-cube .swiper-slide .swiper-slide{pointer-events:none}.swiper-container-cube.swiper-container-rtl .swiper-slide{-webkit-transform-origin:100% 0;transform-origin:100% 0}.swiper-container-cube .swiper-slide-active,.swiper-container-cube .swiper-slide-active .swiper-slide-active{pointer-events:auto}.swiper-container-cube .swiper-slide-active,.swiper-container-cube .swiper-slide-next,.swiper-container-cube .swiper-slide-next+.swiper-slide,.swiper-container-cube .swiper-slide-prev{pointer-events:auto;visibility:visible}.swiper-container-cube .swiper-slide-shadow-bottom,.swiper-container-cube .swiper-slide-shadow-left,.swiper-container-cube .swiper-slide-shadow-right,.swiper-container-cube .swiper-slide-shadow-top{z-index:0;-webkit-backface-visibility:hidden;backface-visibility:hidden}.swiper-container-cube .swiper-cube-shadow{position:absolute;left:0;bottom:0px;width:100%;height:100%;opacity:.6;z-index:0}.swiper-container-cube .swiper-cube-shadow:before{content:'';background:#000;position:absolute;left:0;top:0;bottom:0;right:0;-webkit-filter:blur(50px);filter:blur(50px)}.swiper-container-flip{overflow:visible}.swiper-container-flip .swiper-slide{pointer-events:none;-webkit-backface-visibility:hidden;backface-visibility:hidden;z-index:1}.swiper-container-flip .swiper-slide .swiper-slide{pointer-events:none}.swiper-container-flip .swiper-slide-active,.swiper-container-flip .swiper-slide-active .swiper-slide-active{pointer-events:auto}.swiper-container-flip .swiper-slide-shadow-bottom,.swiper-container-flip .swiper-slide-shadow-left,.swiper-container-flip .swiper-slide-shadow-right,.swiper-container-flip .swiper-slide-shadow-top{z-index:0;-webkit-backface-visibility:hidden;backface-visibility:hidden}taro-swiper-core{display:block;height:150px}taro-swiper-core .swiper-container{height:100%}taro-swiper-core .swiper-pagination{font-size:0}taro-swiper-core .swiper-pagination.swiper-pagination-bullet{opacity:1}taro-swiper-core .swiper-pagination.swiper-pagination-hidden{display:none}";
                    },
                  },
                ]
              )
            );
          })(c.wt),
          [
            4,
            "taro-swiper-core",
            {
              indicatorDots: [4, "indicator-dots"],
              indicatorColor: [1, "indicator-color"],
              indicatorActiveColor: [1, "indicator-active-color"],
              autoplay: [4],
              current: [2],
              interval: [2],
              duration: [2],
              circular: [4],
              vertical: [4],
              previousMargin: [1, "previous-margin"],
              nextMargin: [1, "next-margin"],
              displayMultipleItems: [2, "display-multiple-items"],
              full: [4],
              zoom: [4],
              swiperWrapper: [32],
              swiper: [32],
              isWillLoadCalled: [32],
              source: [32],
              observer: [32],
              observerFirst: [32],
              observerLast: [32],
            },
          ]
        );
      var se = function defineCustomElement$1() {
        if ("undefined" != typeof customElements) {
          ["taro-swiper-core"].forEach(function (e) {
            if ("taro-swiper-core" === e)
              customElements.get(e) || customElements.define(e, oe);
          });
        }
      };
    },
    4308: function (e, t, n) {
      "use strict";
      n.d(t, {
        X: function () {
          return d;
        },
      });
      var i = n(3029),
        r = n(2901),
        a = n(6919),
        o = n(5501),
        s = n(2902),
        l = Node.prototype.cloneNode;
      function cloneNode(e, t) {
        var n = l.call(e, !1),
          i = (function childNodes(e) {
            var t = e.childNodes;
            if (e["s-sc"]) {
              for (var n = [], i = 0; i < t.length; i++) {
                var r = t[i]["s-nr"];
                r && n.push(r);
              }
              return n;
            }
            return Array.from(t);
          })(e);
        if (t)
          for (var r = 0; r < i.length; r++) {
            var a = i[r],
              o = t;
            if (2 !== a.nodeType && 8 !== a.nodeType) {
              a["s-cr"] || (o = !1);
              var s = cloneNode(a, o);
              n.appendChild(s);
            }
          }
        return n;
      }
      var c = (0, s.w$)(
        (function (e) {
          function _class() {
            var e;
            return (
              (0, i.A)(this, _class),
              (e = (0, a.A)(this, _class)).__registerHost(),
              (e.itemId = void 0),
              e
            );
          }
          return (
            (0, o.A)(_class, e),
            (0, r.A)(_class, [
              {
                key: "componentDidRender",
                value: function componentDidRender() {
                  var e = this;
                  this.el.cloneNode = function () {
                    var t =
                      arguments.length > 0 &&
                      void 0 !== arguments[0] &&
                      arguments[0];
                    return cloneNode.call(null, e.el, t);
                  };
                },
              },
              {
                key: "render",
                value: function render() {
                  return (0, s.h)(s.xr, {
                    class: "swiper-slide",
                    "item-id": this.itemId,
                  });
                },
              },
              {
                key: "el",
                get: function get() {
                  return this;
                },
              },
            ])
          );
        })(s.wt),
        [
          0,
          "taro-swiper-item-core",
          {
            itemId: [1, "item-id"],
          },
        ]
      );
      var d = function defineCustomElement$1() {
        if ("undefined" != typeof customElements) {
          ["taro-swiper-item-core"].forEach(function (e) {
            if ("taro-swiper-item-core" === e)
              customElements.get(e) || customElements.define(e, c);
          });
        }
      };
    },
    7289: function (e, t, n) {
      "use strict";
      n.d(t, {
        eB: function () {
          return se;
        },
        $n: function () {
          return le;
        },
        Hl: function () {
          return ce;
        },
        RW: function () {
          return de;
        },
        lV: function () {
          return ue;
        },
        In: function () {
          return pe;
        },
        _V: function () {
          return he;
        },
        pd: function () {
          return me;
        },
        mD: function () {
          return fe;
        },
        sD: function () {
          return ve;
        },
        BM: function () {
          return ge;
        },
        RC: function () {
          return we;
        },
        wu: function () {
          return be;
        },
        EY: function () {
          return ye;
        },
        Ce: function () {
          return Ae;
        },
        Ss: function () {
          return xe;
        },
      });
      var i = n(6540),
        r = function manipulatePropsFunction(e) {
          var t =
              arguments.length > 1 && void 0 !== arguments[1]
                ? arguments[1]
                : {},
            n = e.dangerouslySetInnerHTML,
            i = e.style;
          return (
            "string" != typeof i && (t.style = i),
            Object.assign(Object.assign({}, t), {
              dangerouslySetInnerHTML: n,
            })
          );
        },
        a = n(4467),
        o = n(3029),
        s = n(2901),
        l = n(6919),
        c = n(5501),
        d = n(2902),
        u = n(6349),
        p = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.onSubmit = (0, d.lh)(e, "tarobuttonsubmit", 7)),
                (e.onReset = (0, d.lh)(e, "tarobuttonreset", 7)),
                (e.disabled = void 0),
                (e.hoverClass = "button-hover"),
                (e.type = ""),
                (e.hoverStartTime = 20),
                (e.hoverStayTime = 70),
                (e.size = void 0),
                (e.plain = void 0),
                (e.loading = !1),
                (e.formType = null),
                (e.hover = !1),
                (e.touch = !1),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(
                _class,
                [
                  {
                    key: "onClick",
                    value: function onClick(e) {
                      this.disabled && e.stopPropagation();
                    },
                  },
                  {
                    key: "onTouchStart",
                    value: function onTouchStart() {
                      var e = this;
                      this.disabled ||
                        ((this.touch = !0),
                        this.hoverClass &&
                          !this.disabled &&
                          setTimeout(function () {
                            e.touch && (e.hover = !0);
                          }, this.hoverStartTime));
                    },
                  },
                  {
                    key: "onTouchEnd",
                    value: function onTouchEnd() {
                      var e = this;
                      this.disabled ||
                        ((this.touch = !1),
                        this.hoverClass &&
                          !this.disabled &&
                          setTimeout(function () {
                            e.touch || (e.hover = !1);
                          }, this.hoverStayTime),
                        "submit" === this.formType
                          ? this.onSubmit.emit()
                          : "reset" === this.formType && this.onReset.emit());
                    },
                  },
                  {
                    key: "render",
                    value: function render() {
                      var e = this.disabled,
                        t = this.hoverClass,
                        n = this.type,
                        i = this.size,
                        r = this.plain,
                        o = this.loading,
                        s = this.hover,
                        l = (0, u.c)((0, a.A)({}, "".concat(t), s && !e));
                      return (0, d.h)(
                        d.xr,
                        {
                          class: l,
                          type: n,
                          plain: r,
                          loading: o,
                          size: i,
                          disabled: e,
                        },
                        o &&
                          (0, d.h)("i", {
                            class: "weui-loading",
                          }),
                        (0, d.h)("slot", null)
                      );
                    },
                  },
                  {
                    key: "el",
                    get: function get() {
                      return this;
                    },
                  },
                ],
                [
                  {
                    key: "style",
                    get: function get() {
                      return 'taro-button-core{display:block;overflow:hidden;position:relative;-webkit-box-sizing:border-box;box-sizing:border-box;margin-left:auto;margin-right:auto;padding-left:14px;padding-right:14px;border-width:0;border-radius:5px;width:100%;-webkit-appearance:none;-moz-appearance:none;appearance:none;outline:0;background-color:#f8f8f8;line-height:2.55555556;text-decoration:none;text-align:center;font-size:18px;color:#000;-webkit-tap-highlight-color:rgba(0, 0, 0, 0)}taro-button-core:focus{outline:0}taro-button-core:not([disabled]):active{background-color:#dedede;color:rgba(0, 0, 0, 0.6)}taro-button-core::after{position:absolute;left:0;top:0;-webkit-box-sizing:border-box;box-sizing:border-box;border:1px solid rgba(0, 0, 0, 0.2);border-radius:10px;width:200%;height:200%;content:" ";-webkit-transform:scale(0.5);transform:scale(0.5);-webkit-transform-origin:0 0;transform-origin:0 0}taro-button-core+taro-button-core{margin-top:15px}taro-button-core[type=default]{background-color:#f8f8f8;color:#000}taro-button-core[type=default]:not([disabled]):visited{color:#000}taro-button-core[type=default]:not([disabled]):active{background-color:#dedede;color:rgba(0, 0, 0, 0.6)}taro-button-core[size=mini]{display:inline-block;padding:0 1.32em;width:auto;line-height:2.3;font-size:13px}taro-button-core[plain],taro-button-core[plain][type=default],taro-button-core[plain][type=primary]{border-width:1px;background-color:transparent}taro-button-core[disabled]{color:rgba(255, 255, 255, 0.6)}taro-button-core[disabled][type=default]{background-color:#f7f7f7;color:rgba(0, 0, 0, 0.3)}taro-button-core[disabled][type=primary]{background-color:#9ed99d}taro-button-core[disabled][type=warn]{background-color:#ec8b89}taro-button-core[loading] .weui-loading{margin:-0.2em 0.34em 0 0}taro-button-core[loading][type=primary],taro-button-core[loading][type=warn]{color:rgba(255, 255, 255, 0.6)}taro-button-core[loading][type=primary]{background-color:#179b16}taro-button-core[loading][type=warn]{background-color:#ce3c39}taro-button-core[plain][type=primary]{border:1px solid #1aad19;color:#1aad19}taro-button-core[plain][type=primary]:not([disabled]):active{border-color:rgba(26, 173, 25, 0.6);background-color:transparent;color:rgba(26, 173, 25, 0.6)}taro-button-core[plain][type=primary]::after{border-width:0}taro-button-core[plain][type=warn]{border:1px solid #e64340;color:#e64340}taro-button-core[plain][type=warn]:not([disabled]):active{border-color:rgba(230, 67, 64, 0.6);background-color:transparent;color:rgba(230, 67, 64, 0.6)}taro-button-core[plain][type=warn]::after{border-width:0}taro-button-core[plain],taro-button-core[plain][type=default]{border:1px solid #353535;color:#353535}taro-button-core[plain]:not([disabled]):active,taro-button-core[plain][type=default]:not([disabled]):active{border-color:rgba(53, 53, 53, 0.6);background-color:transparent;color:rgba(53, 53, 53, 0.6)}taro-button-core[plain]::after,taro-button-core[plain][type=default]::after{border-width:0}taro-button-core[type=primary]{background-color:#1aad19;color:#fff}taro-button-core[type=primary]:not([disabled]):visited{color:#fff}taro-button-core[type=primary]:not([disabled]):active{background-color:#179b16;color:rgba(255, 255, 255, 0.6)}taro-button-core[type=warn]{background-color:#e64340;color:#fff}taro-button-core[type=warn]:not([disabled]):visited{color:#fff}taro-button-core[type=warn]:not([disabled]):active{background-color:#ce3c39;color:rgba(255, 255, 255, 0.6)}taro-button-core[plain][disabled]{border:1px solid rgba(0, 0, 0, 0.2);background-color:#f7f7f7;color:rgba(0, 0, 0, 0.3)}taro-button-core[plain][disabled][type=primary]{border:1px solid rgba(0, 0, 0, 0.2);background-color:#f7f7f7;color:rgba(0, 0, 0, 0.3)}';
                    },
                  },
                ]
              )
            );
          })(d.wt),
          [
            4,
            "taro-button-core",
            {
              disabled: [4],
              hoverClass: [1, "hover-class"],
              type: [1],
              hoverStartTime: [2, "hover-start-time"],
              hoverStayTime: [2, "hover-stay-time"],
              size: [1],
              plain: [4],
              loading: [4],
              formType: [513, "form-type"],
              hover: [32],
              touch: [32],
            },
            [
              [0, "click", "onClick"],
              [1, "touchstart", "onTouchStart"],
              [1, "touchend", "onTouchEnd"],
            ],
          ]
        );
      var h = function defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-button-core"].forEach(function (e) {
              if ("taro-button-core" === e)
                customElements.get(e) || customElements.define(e, p);
            });
          }
        },
        m = n(5544),
        f = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.onLongTap = (0, d.lh)(e, "longtap", 7)),
                (e.onTouchStart = function () {
                  e.timer = setTimeout(function () {
                    e.onLongTap.emit();
                  }, 500);
                }),
                (e.onTouchMove = function () {
                  clearTimeout(e.timer);
                }),
                (e.onTouchEnd = function () {
                  clearTimeout(e.timer);
                }),
                (e.canvasId = void 0),
                (e.height = void 0),
                (e.width = void 0),
                (e.nativeProps = {}),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(
                _class,
                [
                  {
                    key: "componentDidRender",
                    value: function componentDidRender() {
                      var e = (0, m.A)(this.el.children, 1)[0];
                      if (!this.height || !this.width) {
                        var t = window.getComputedStyle(e);
                        this.height || (this.height = t.height),
                          this.width || (this.width = t.width);
                      }
                      (e.height = parseInt(this.height)),
                        (e.width = parseInt(this.width));
                    },
                  },
                  {
                    key: "render",
                    value: function render() {
                      var e = this.canvasId,
                        t = this.nativeProps;
                      return (0, d.h)(
                        "canvas",
                        Object.assign(
                          {
                            "canvas-id": e,
                            style: {
                              width: "100%",
                              height: "100%",
                            },
                            onTouchStart: this.onTouchStart,
                            onTouchMove: this.onTouchMove,
                            onTouchCancel: this.onTouchEnd,
                            onTouchEnd: this.onTouchEnd,
                          },
                          t
                        )
                      );
                    },
                  },
                  {
                    key: "el",
                    get: function get() {
                      return this;
                    },
                  },
                ],
                [
                  {
                    key: "style",
                    get: function get() {
                      return "taro-canvas-core{display:block;position:relative;width:300px;height:150px}";
                    },
                  },
                ]
              )
            );
          })(d.wt),
          [
            0,
            "taro-canvas-core",
            {
              canvasId: [1, "id"],
              height: [1537],
              width: [1537],
              nativeProps: [16],
            },
          ]
        );
      var v = function taro_canvas_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-canvas-core"].forEach(function (e) {
              if ("taro-canvas-core" === e)
                customElements.get(e) || customElements.define(e, f);
            });
          }
        },
        g = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(_class, [
                {
                  key: "render",
                  value: function render() {
                    return (0, d.h)(d.xr, null);
                  },
                },
              ])
            );
          })(d.wt),
          [0, "taro-custom-wrapper-core"]
        );
      var w,
        b = function taro_custom_wrapper_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-custom-wrapper-core"].forEach(function (e) {
              if ("taro-custom-wrapper-core" === e)
                customElements.get(e) || customElements.define(e, g);
            });
          }
        },
        __classPrivateFieldSet = function (e, t, n, i, r) {
          if ("m" === i) throw new TypeError("Private method is not writable");
          if ("a" === i && !r)
            throw new TypeError(
              "Private accessor was defined without a setter"
            );
          if ("function" == typeof t ? e !== t || !r : !t.has(e))
            throw new TypeError(
              "Cannot write private member to an object whose class did not declare it"
            );
          return "a" === i ? r.call(e, n) : r ? (r.value = n) : t.set(e, n), n;
        },
        __classPrivateFieldGet = function (e, t, n, i) {
          if ("a" === n && !i)
            throw new TypeError(
              "Private accessor was defined without a getter"
            );
          if ("function" == typeof t ? e !== t || !i : !t.has(e))
            throw new TypeError(
              "Cannot read private member from an object whose class did not declare it"
            );
          return "m" === n ? i : "a" === n ? i.call(e) : i ? i.value : t.get(e);
        },
        y = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.onSubmit = (0, d.lh)(e, "submit", 7)),
                w.set(e, {}),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(_class, [
                {
                  key: "onButtonSubmit",
                  value: function onButtonSubmit(e) {
                    e.stopPropagation(),
                      __classPrivateFieldSet(this, w, this.getFormValue(), "f"),
                      this.onSubmit.emit({
                        value: __classPrivateFieldGet(this, w, "f"),
                      });
                  },
                },
                {
                  key: "onButtonReset",
                  value: function onButtonReset(e) {
                    e.stopPropagation(), this.form.reset();
                  },
                },
                {
                  key: "componentDidLoad",
                  value: function componentDidLoad() {
                    var e = this;
                    __classPrivateFieldSet(this, w, this.getFormValue(), "f"),
                      Object.defineProperty(this.el, "value", {
                        get: function get() {
                          return __classPrivateFieldGet(e, w, "f");
                        },
                        configurable: !0,
                      });
                  },
                },
                {
                  key: "componentDidRender",
                  value: function componentDidRender() {
                    var e = this;
                    if (
                      (this.originalAppendChild ||
                        ((this.originalAppendChild = this.el.appendChild),
                        (this.originalInsertBefore = this.el.insertBefore),
                        (this.originalReplaceChild = this.el.replaceChild),
                        (this.originalRemoveChild = this.el.removeChild)),
                      !this.form)
                    )
                      return (
                        (this.el.appendChild = this.originalAppendChild),
                        (this.el.insertBefore = this.originalInsertBefore),
                        (this.el.replaceChild = this.originalReplaceChild),
                        void (this.el.removeChild = this.originalRemoveChild)
                      );
                    (this.el.appendChild = function (t) {
                      return e.form.appendChild(t);
                    }),
                      (this.el.insertBefore = function (t, n) {
                        return e.form.insertBefore(t, n);
                      }),
                      (this.el.replaceChild = function (t, n) {
                        return e.form.replaceChild(t, n);
                      }),
                      (this.el.removeChild = function (t) {
                        return e.form.removeChild(t);
                      });
                  },
                },
                {
                  key: "getFormValue",
                  value: function getFormValue() {
                    for (
                      var e = this.el,
                        t = [],
                        n = e.getElementsByTagName("input"),
                        i = 0;
                      i < n.length;
                      i++
                    )
                      t.push(n[i]);
                    var r = {},
                      a = {};
                    t.forEach(function (e) {
                      "string" == typeof e.name &&
                        (-1 === e.className.indexOf("weui-switch")
                          ? "radio" !== e.type
                            ? "checkbox" !== e.type
                              ? (r[e.name] = e.value)
                              : e.checked
                              ? a[e.name]
                                ? r[e.name].push(e.value)
                                : ((a[e.name] = !0), (r[e.name] = [e.value]))
                              : a[e.name] || (r[e.name] = [])
                            : e.checked
                            ? ((a[e.name] = !0), (r[e.name] = e.value))
                            : a[e.name] || (r[e.name] = "")
                          : (r[e.name] = e.checked));
                    });
                    for (
                      var o = e.getElementsByTagName("textarea"), s = [], l = 0;
                      l < o.length;
                      l++
                    )
                      s.push(o[l]);
                    return (
                      s.forEach(function (e) {
                        "string" == typeof e.name && (r[e.name] = e.value);
                      }),
                      r
                    );
                  },
                },
                {
                  key: "render",
                  value: function render() {
                    var e = this;
                    return (0, d.h)(
                      "form",
                      {
                        ref: function ref(t) {
                          e.form = t;
                        },
                      },
                      (0, d.h)("slot", null)
                    );
                  },
                },
                {
                  key: "el",
                  get: function get() {
                    return this;
                  },
                },
              ])
            );
          })(d.wt),
          [
            4,
            "taro-form-core",
            void 0,
            [
              [0, "tarobuttonsubmit", "onButtonSubmit"],
              [0, "tarobuttonreset", "onButtonReset"],
            ],
          ]
        );
      w = new WeakMap();
      var A = function taro_form_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-form-core"].forEach(function (e) {
              if ("taro-form-core" === e)
                customElements.get(e) || customElements.define(e, y);
            });
          }
        },
        x = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.type = void 0),
                (e.size = "23"),
                (e.color = void 0),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(_class, [
                {
                  key: "render",
                  value: function render() {
                    var e = this.type,
                      t = this.size,
                      n = this.color,
                      i = null == e ? void 0 : e.replace(/_/g, "-"),
                      r = (0, u.c)((0, a.A)({}, "weui-icon-".concat(i), !0)),
                      o = {
                        "font-size": "".concat(t, "px"),
                        color: n,
                      };
                    return (0, d.h)(d.xr, {
                      class: r,
                      style: o,
                    });
                  },
                },
              ])
            );
          })(d.wt),
          [
            0,
            "taro-icon-core",
            {
              type: [1],
              size: [8],
              color: [1],
            },
          ]
        );
      var _ = function taro_icon_core_defineCustomElement$1() {
        if ("undefined" != typeof customElements) {
          ["taro-icon-core"].forEach(function (e) {
            if ("taro-icon-core" === e)
              customElements.get(e) || customElements.define(e, x);
          });
        }
      };
      n.e(9941).then(n.t.bind(n, 9941, 23));
      var E = (0, d.w$)(
        (function (e) {
          function _class() {
            var e;
            return (
              (0, o.A)(this, _class),
              (e = (0, l.A)(this, _class)).__registerHost(),
              (e.onLoad = (0, d.lh)(e, "load", 7)),
              (e.onError = (0, d.lh)(e, "error", 7)),
              (e.src = void 0),
              (e.mode = "scaleToFill"),
              (e.lazyLoad = !1),
              (e.nativeProps = {}),
              (e.aspectFillMode = "width"),
              (e.didLoad = !1),
              e
            );
          }
          return (
            (0, c.A)(_class, e),
            (0, s.A)(
              _class,
              [
                {
                  key: "componentDidLoad",
                  value: function componentDidLoad() {
                    var e = this;
                    if (this.lazyLoad) {
                      var t = new IntersectionObserver(
                        function (n) {
                          n[n.length - 1].isIntersecting &&
                            (t.unobserve(e.imgRef), (e.didLoad = !0));
                        },
                        {
                          rootMargin: "300px 0px",
                        }
                      );
                      t.observe(this.imgRef);
                    }
                  },
                },
                {
                  key: "imageOnLoad",
                  value: function imageOnLoad() {
                    var e = this.imgRef,
                      t = e.width,
                      n = e.height,
                      i = e.naturalWidth,
                      r = e.naturalHeight;
                    this.onLoad.emit({
                      width: t,
                      height: n,
                    }),
                      (this.aspectFillMode = i > r ? "width" : "height");
                  },
                },
                {
                  key: "imageOnError",
                  value: function imageOnError(e) {
                    this.onError.emit(e);
                  },
                },
                {
                  key: "render",
                  value: function render() {
                    var e = this,
                      t = this.src,
                      n = this.mode,
                      i = void 0 === n ? "scaleToFill" : n,
                      r = this.lazyLoad,
                      o = void 0 !== r && r,
                      s = this.aspectFillMode,
                      l = void 0 === s ? "width" : s,
                      c = this.imageOnLoad,
                      p = this.imageOnError,
                      h = this.nativeProps,
                      m = this.didLoad,
                      f = (0, u.c)({
                        "taro-img__widthfix": "widthFix" === i,
                      }),
                      v = (0, u.c)(
                        "taro-img__mode-".concat(
                          i.toLowerCase().replace(/\s/g, "")
                        ),
                        (0, a.A)(
                          {},
                          "taro-img__mode-aspectfill--".concat(l),
                          "aspectFill" === i
                        )
                      );
                    return (0, d.h)(
                      d.xr,
                      {
                        class: f,
                      },
                      (0, d.h)(
                        "img",
                        Object.assign(
                          {
                            ref: function ref(t) {
                              return (e.imgRef = t);
                            },
                            class: v,
                            src: o && !m ? void 0 : t,
                            onLoad: c.bind(this),
                            onError: p.bind(this),
                          },
                          h
                        )
                      )
                    );
                  },
                },
              ],
              [
                {
                  key: "style",
                  get: function get() {
                    return 'img[src=""]{opacity:0}taro-image-core{display:inline-block;overflow:hidden;position:relative;width:auto;height:auto;font-size:0}.taro-img.taro-img__widthfix{height:100%}.taro-img__mode-scaletofill{width:100%;height:100%}.taro-img__mode-aspectfit{max-width:100%;max-height:100%}.taro-img__mode-aspectfill{position:absolute;left:50%;top:50%;-webkit-transform:translate(-50%, -50%);transform:translate(-50%, -50%)}.taro-img__mode-aspectfill--width{min-width:100%;height:100%}.taro-img__mode-aspectfill--height{width:100%;min-height:100%}.taro-img__mode-widthfix{width:100%}.taro-img__mode-heightfix{height:100%}.taro-img__mode-top{width:100%}.taro-img__mode-bottom{position:absolute;bottom:0;width:100%}.taro-img__mode-left{height:100%}.taro-img__mode-right{position:absolute;right:0;height:100%}.taro-img__mode-topright{position:absolute;right:0}.taro-img__mode-bottomleft{position:absolute;bottom:0}.taro-img__mode-bottomright{position:absolute;right:0;bottom:0}';
                  },
                },
              ]
            )
          );
        })(d.wt),
        [
          0,
          "taro-image-core",
          {
            src: [1],
            mode: [1],
            lazyLoad: [4, "lazy-load"],
            nativeProps: [16],
            aspectFillMode: [32],
            didLoad: [32],
          },
        ]
      );
      var S = function taro_image_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-image-core"].forEach(function (e) {
              if ("taro-image-core" === e)
                customElements.get(e) || customElements.define(e, E);
            });
          }
        },
        C = n(675),
        T = n(467);
      function getTrueType(e, t, n) {
        if (
          ("search" === t && (e = "search"),
          n && (e = "password"),
          void 0 === e)
        )
          return "text";
        if (!e) throw new Error("unexpected type");
        return "digit" === e && (e = "number"), e;
      }
      function fixControlledValue(e) {
        return null != e ? e : "";
      }
      var k = (0, d.w$)(
        (function (e) {
          function _class() {
            var e;
            return (
              (0, o.A)(this, _class),
              (e = (0, l.A)(this, _class)).__registerHost(),
              (e.onInput = (0, d.lh)(e, "input", 7)),
              (e.onPaste = (0, d.lh)(e, "paste", 7)),
              (e.onFocus = (0, d.lh)(e, "focus", 7)),
              (e.onBlur = (0, d.lh)(e, "blur", 7)),
              (e.onConfirm = (0, d.lh)(e, "confirm", 7)),
              (e.onChange = (0, d.lh)(e, "change", 7)),
              (e.onKeyDown = (0, d.lh)(e, "keydown", 7)),
              (e.isOnComposition = !1),
              (e.isOnPaste = !1),
              (e.onInputExcuted = !1),
              (e.handleInput = function (t) {
                t.stopPropagation();
                var n = e,
                  i = n.type,
                  r = n.maxlength,
                  a = n.confirmType,
                  o = n.password;
                if (!e.isOnComposition && !e.onInputExcuted) {
                  var s = t.target.value,
                    l = getTrueType(i, a, o);
                  (e.onInputExcuted = !0),
                    "number" === l &&
                      s &&
                      r > -1 &&
                      r <= s.length &&
                      ((s = s.substring(0, r)), (t.target.value = s)),
                    (e.value = s),
                    e.onInput.emit({
                      value: s,
                      cursor: s.length,
                    }),
                    (e.onInputExcuted = !1);
                }
              }),
              (e.handlePaste = function (t) {
                t.stopPropagation(),
                  (e.isOnPaste = !0),
                  e.onPaste.emit({
                    value: t.target.value,
                  });
              }),
              (e.handleFocus = function (t) {
                t.stopPropagation(),
                  (e.onInputExcuted = !1),
                  e.onFocus.emit({
                    value: t.target.value,
                  });
              }),
              (e.handleBlur = function (t) {
                t.stopPropagation(),
                  e.onBlur.emit({
                    value: t.target.value,
                  });
              }),
              (e.handleChange = function (t) {
                t.stopPropagation(),
                  e.onChange.emit({
                    value: t.target.value,
                  }),
                  e.isOnPaste &&
                    ((e.isOnPaste = !1),
                    (e.value = t.target.value),
                    e.onInput.emit({
                      value: t.target.value,
                      cursor: t.target.value.length,
                    }));
              }),
              (e.handleKeyDown = function (t) {
                t.stopPropagation();
                var n = t.target.value,
                  i = t.keyCode || t.code;
                (e.onInputExcuted = !1),
                  e.onKeyDown.emit({
                    value: n,
                    cursor: n.length,
                    keyCode: i,
                  }),
                  13 === i &&
                    e.onConfirm.emit({
                      value: n,
                    });
              }),
              (e.handleComposition = function (t) {
                t.stopPropagation(),
                  t.target instanceof HTMLInputElement &&
                    ("compositionend" === t.type
                      ? ((e.isOnComposition = !1),
                        (e.value = t.target.value),
                        e.onInput.emit({
                          value: t.target.value,
                          cursor: t.target.value.length,
                        }))
                      : (e.isOnComposition = !0));
              }),
              (e.handleBeforeInput = function (t) {
                if (t.data) {
                  var n = t.data && /[0-9]/.test(t.data);
                  "number" !== e.type || n || t.preventDefault(),
                    "digit" !== e.type ||
                      n ||
                      (("." !== t.data ||
                        ("." === t.data && t.target.value.indexOf(".") > -1)) &&
                        t.preventDefault());
                }
              }),
              (e.value = ""),
              (e.type = void 0),
              (e.password = !1),
              (e.placeholder = void 0),
              (e.disabled = !1),
              (e.maxlength = 140),
              (e.autoFocus = !1),
              (e.confirmType = "done"),
              (e.name = void 0),
              (e.nativeProps = {}),
              e
            );
          }
          return (
            (0, c.A)(_class, e),
            (0, s.A)(
              _class,
              [
                {
                  key: "focus",
                  value:
                    ((t = (0, T.A)(
                      (0, C.A)().mark(function _callee() {
                        return (0, C.A)().wrap(
                          function _callee$(e) {
                            for (;;)
                              switch ((e.prev = e.next)) {
                                case 0:
                                  this.inputRef.focus();
                                case 1:
                                case "end":
                                  return e.stop();
                              }
                          },
                          _callee,
                          this
                        );
                      })
                    )),
                    function focus() {
                      return t.apply(this, arguments);
                    }),
                },
                {
                  key: "watchAutoFocus",
                  value: function watchAutoFocus(e, t) {
                    var n;
                    !t &&
                      e &&
                      (null === (n = this.inputRef) ||
                        void 0 === n ||
                        n.focus());
                  },
                },
                {
                  key: "watchValue",
                  value: function watchValue(e) {
                    var t = fixControlledValue(e);
                    this.inputRef.value !== t && (this.inputRef.value = t);
                  },
                },
                {
                  key: "componentDidLoad",
                  value: function componentDidLoad() {
                    var e,
                      t,
                      n,
                      i,
                      r,
                      a = this;
                    "file" === this.type
                      ? ((this.fileListener = function () {
                          a.onInput.emit();
                        }),
                        null === (e = this.inputRef) ||
                          void 0 === e ||
                          e.addEventListener("change", this.fileListener))
                      : (null === (t = this.inputRef) ||
                          void 0 === t ||
                          t.addEventListener(
                            "compositionstart",
                            this.handleComposition
                          ),
                        null === (n = this.inputRef) ||
                          void 0 === n ||
                          n.addEventListener(
                            "compositionend",
                            this.handleComposition
                          ),
                        null === (i = this.inputRef) ||
                          void 0 === i ||
                          i.addEventListener(
                            "beforeinput",
                            this.handleBeforeInput
                          ),
                        null === (r = this.inputRef) ||
                          void 0 === r ||
                          r.addEventListener(
                            "textInput",
                            this.handleBeforeInput
                          ));
                  },
                },
                {
                  key: "disconnectedCallback",
                  value: function disconnectedCallback() {
                    var e, t, n, i, r;
                    "file" === this.type
                      ? null === (e = this.inputRef) ||
                        void 0 === e ||
                        e.removeEventListener("change", this.fileListener)
                      : (null === (t = this.inputRef) ||
                          void 0 === t ||
                          t.removeEventListener(
                            "compositionstart",
                            this.handleComposition
                          ),
                        null === (n = this.inputRef) ||
                          void 0 === n ||
                          n.removeEventListener(
                            "compositionend",
                            this.handleComposition
                          ),
                        null === (i = this.inputRef) ||
                          void 0 === i ||
                          i.removeEventListener(
                            "beforeinput",
                            this.handleBeforeInput
                          ),
                        null === (r = this.inputRef) ||
                          void 0 === r ||
                          r.removeEventListener(
                            "textInput",
                            this.handleBeforeInput
                          ));
                  },
                },
                {
                  key: "render",
                  value: function render() {
                    var e = this,
                      t = this.value,
                      n = this.type,
                      i = this.password,
                      r = this.placeholder,
                      a = this.autoFocus,
                      o = this.disabled,
                      s = this.maxlength,
                      l = this.confirmType,
                      c = this.name,
                      u = this.nativeProps;
                    return (0, d.h)(
                      "input",
                      Object.assign(
                        {
                          ref: function ref(t) {
                            (e.inputRef = t), a && t && t.focus();
                          },
                          class: "weui-input",
                          type: getTrueType(n, l, i),
                          placeholder: r,
                          autoFocus: a,
                          disabled: o,
                          maxlength: s,
                          name: c,
                          onInput: this.handleInput,
                          onFocus: this.handleFocus,
                          onBlur: this.handleBlur,
                          onChange: this.handleChange,
                          onKeyDown: this.handleKeyDown,
                          onPaste: this.handlePaste,
                          onCompositionStart: this.handleComposition,
                          onCompositionEnd: this.handleComposition,
                        },
                        u,
                        {
                          value: fixControlledValue(t),
                        }
                      )
                    );
                  },
                },
                {
                  key: "el",
                  get: function get() {
                    return this;
                  },
                },
              ],
              [
                {
                  key: "watchers",
                  get: function get() {
                    return {
                      autoFocus: ["watchAutoFocus"],
                      value: ["watchValue"],
                    };
                  },
                },
                {
                  key: "style",
                  get: function get() {
                    return "taro-input-core{display:block}input{display:block;overflow:hidden;height:1.4rem;text-overflow:clip;text-align:inherit;white-space:nowrap}";
                  },
                },
              ]
            )
          );
          var t;
        })(d.wt),
        [
          0,
          "taro-input-core",
          {
            value: [1025],
            type: [1],
            password: [4],
            placeholder: [1],
            disabled: [4],
            maxlength: [2],
            autoFocus: [4, "focus"],
            confirmType: [1, "confirm-type"],
            name: [1],
            nativeProps: [16],
            focus: [64],
          },
        ]
      );
      var M = function taro_input_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-input-core"].forEach(function (e) {
              if ("taro-input-core" === e)
                customElements.get(e) || customElements.define(e, k);
            });
          }
        },
        I = n(4025),
        L = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.onSuccess = (0, d.lh)(e, "cuccess", 7)),
                (e.onFail = (0, d.lh)(e, "fail", 7)),
                (e.onComplete = (0, d.lh)(e, "complete", 7)),
                (e.hoverClass = void 0),
                (e.url = void 0),
                (e.openType = "navigate"),
                (e.isHover = !1),
                (e.delta = 0),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(
                _class,
                [
                  {
                    key: "onClick",
                    value: function onClick() {
                      var e = this.openType,
                        t = this.onSuccess,
                        n = this.onFail,
                        i = this.onComplete,
                        r = Promise.resolve();
                      switch (e) {
                        case "navigate":
                          r = I.Ay.navigateTo({
                            url: this.url,
                          });
                          break;
                        case "redirect":
                          r = I.Ay.redirectTo({
                            url: this.url,
                          });
                          break;
                        case "switchTab":
                          r = I.Ay.switchTab({
                            url: this.url,
                          });
                          break;
                        case "reLaunch":
                          r = I.Ay.reLaunch({
                            url: this.url,
                          });
                          break;
                        case "navigateBack":
                          r = I.Ay.navigateBack({
                            delta: this.delta,
                          });
                          break;
                        case "exit":
                          r = Promise.reject(
                            new Error('navigator:fail 暂不支持"openType: exit"')
                          );
                      }
                      r &&
                        r
                          .then(function (e) {
                            t.emit(e);
                          })
                          .catch(function (e) {
                            n.emit(e);
                          })
                          .finally(function () {
                            i.emit();
                          });
                    },
                  },
                  {
                    key: "render",
                    value: function render() {
                      var e = this.isHover,
                        t = this.hoverClass;
                      return (0, d.h)(d.xr, {
                        class: (0, u.c)((0, a.A)({}, t, e)),
                      });
                    },
                  },
                ],
                [
                  {
                    key: "style",
                    get: function get() {
                      return ".navigator-hover{background:#efefef}";
                    },
                  },
                ]
              )
            );
          })(d.wt),
          [
            0,
            "taro-navigator-core",
            {
              hoverClass: [1, "hover-class"],
              url: [1],
              openType: [1, "open-type"],
              isHover: [4, "is-hover"],
              delta: [2],
            },
            [[0, "click", "onClick"]],
          ]
        );
      var P = function taro_navigator_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-navigator-core"].forEach(function (e) {
              if ("taro-navigator-core" === e)
                customElements.get(e) || customElements.define(e, L);
            });
          }
        },
        N = n(2284),
        O = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.renderNode = function (t) {
                  if ("type" in t && "text" === t.type)
                    return (t.text || "").replace(/&nbsp;/g, " ");
                  if ("name" in t && t.name) {
                    var n = t.name,
                      i = t.attrs,
                      r = t.children,
                      a = {},
                      o = [];
                    if (i && "object" === (0, N.A)(i)) {
                      var s = function _loop() {
                        var e = i[l];
                        if ("style" === l && "string" == typeof e) {
                          var t = e
                              .split(";")
                              .map(function (e) {
                                return e.trim();
                              })
                              .filter(function (e) {
                                return e;
                              }),
                            n = {};
                          return (
                            t.forEach(function (e) {
                              if (e) {
                                var t = /(.+): *(.+)/g.exec(e);
                                if (t) {
                                  var i = (0, m.A)(t, 3),
                                    r = i[1],
                                    a = i[2],
                                    o = r.replace(/-([a-z])/g, function () {
                                      for (
                                        var e = arguments.length,
                                          t = new Array(e),
                                          n = 0;
                                        n < e;
                                        n++
                                      )
                                        t[n] = arguments[n];
                                      return t[1].toUpperCase();
                                    });
                                  n[o] = a;
                                }
                              }
                            }),
                            Object.keys(n).length && (a.style = n),
                            1
                          );
                        }
                        a[l] = e;
                      };
                      for (var l in i) s();
                    }
                    return (
                      r &&
                        r.length &&
                        (o = r.map(function (t) {
                          return e.renderNode(t);
                        })),
                      (0, d.h)(n, a, o)
                    );
                  }
                  return null;
                }),
                (e.nodes = void 0),
                (e.selectable = !1),
                (e.userSelect = !1),
                (e.space = void 0),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(
                _class,
                [
                  {
                    key: "render",
                    value: function render() {
                      var e = this.nodes,
                        t = this.renderNode;
                      return Array.isArray(e)
                        ? (0, d.h)(
                            d.xr,
                            null,
                            e.map(function (e) {
                              return t(e);
                            })
                          )
                        : (0, d.h)(d.xr, {
                            innerHTML: e,
                          });
                    },
                  },
                ],
                [
                  {
                    key: "style",
                    get: function get() {
                      return "taro-rich-text-core{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}taro-rich-text-core[selectable=true],taro-rich-text-core[user-select=true]{display:block;-webkit-user-select:text;-moz-user-select:text;-ms-user-select:text;user-select:text}taro-rich-text-core[space]{white-space:pre-wrap}taro-rich-text-core[space=ensp]{word-spacing:0.5em}taro-rich-text-core[space=nbsp]{word-spacing:1em}";
                    },
                  },
                ]
              )
            );
          })(d.wt),
          [
            0,
            "taro-rich-text-core",
            {
              nodes: [1],
              selectable: [1028],
              userSelect: [1028, "user-select"],
              space: [1],
            },
          ]
        );
      var D = function taro_rich_text_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-rich-text-core"].forEach(function (e) {
              if ("taro-rich-text-core" === e)
                customElements.get(e) || customElements.define(e, O);
            });
          }
        },
        z = n(6782);
      function handleStencilNodes(e) {
        var t;
        null === (t = null == e ? void 0 : e.childNodes) ||
          void 0 === t ||
          t.forEach(function (e) {
            e.nodeType === document.COMMENT_NODE &&
              e["s-cn"] &&
              (e["s-cn"] = !1),
              e.nodeType !== document.COMMENT_NODE &&
                e["s-sr"] &&
                (e["s-sr"] = !1);
          });
      }
      function easeOutScroll(e, t) {
        var n =
            arguments.length > 2 && void 0 !== arguments[2]
              ? arguments[2]
              : 500,
          i = arguments.length > 3 ? arguments[3] : void 0;
        if (e !== t && "number" == typeof e) {
          var r = t - e,
            a = Date.now(),
            o = t >= e;
          !(function step() {
            (e = (function linear(e, t, n, i) {
              return (n * e) / i + t;
            })(Date.now() - a, e, r, n)),
              (o && e >= t) || (!o && t >= e)
                ? i(t)
                : (i(e), requestAnimationFrame(step));
          })();
        }
      }
      var j = (0, d.w$)(
        (function (e) {
          function _class() {
            var e;
            return (
              (0, o.A)(this, _class),
              (e = (0, l.A)(this, _class)).__registerHost(),
              (e.onScroll = (0, d.lh)(e, "scroll", 3)),
              (e.onScrollToUpper = (0, d.lh)(e, "scrolltoupper", 3)),
              (e.onScrollToLower = (0, d.lh)(e, "scrolltolower", 3)),
              (e._scrollLeft = 0),
              (e._scrollTop = 0),
              (e.upperAndLower = (0, z.d)(function () {
                var t = e.el,
                  n = t.offsetWidth,
                  i = t.offsetHeight,
                  r = t.scrollLeft,
                  a = t.scrollTop,
                  o = t.scrollHeight,
                  s = t.scrollWidth,
                  l = Number(e.lowerThreshold),
                  c = Number(e.upperThreshold);
                !isNaN(l) &&
                  ((e.scrollY && i + a + l >= o) ||
                    (e.scrollX && n + r + l >= s)) &&
                  e.onScrollToLower.emit({
                    direction: e.scrollX ? "right" : e.scrollY ? "bottom" : "",
                  }),
                  !isNaN(c) &&
                    ((e.scrollY && a <= c) || (e.scrollX && r <= c)) &&
                    e.onScrollToUpper.emit({
                      direction: e.scrollX ? "left" : e.scrollY ? "top" : "",
                    });
              }, 200)),
              (e.scrollX = !1),
              (e.scrollY = !1),
              (e.upperThreshold = 50),
              (e.lowerThreshold = 50),
              (e.mpScrollTop = void 0),
              (e.mpScrollLeft = void 0),
              (e.mpScrollIntoView = void 0),
              (e.mpScrollIntoViewAlignment = void 0),
              (e.animated = !1),
              e
            );
          }
          return (
            (0, c.A)(_class, e),
            (0, s.A)(
              _class,
              [
                {
                  key: "watchScrollLeft",
                  value: function watchScrollLeft(e) {
                    var t = Number(e),
                      n = this.animated;
                    this.mpScrollToMethod({
                      left: t,
                      animated: n,
                    });
                  },
                },
                {
                  key: "watchScrollTop",
                  value: function watchScrollTop(e) {
                    var t = Number(e),
                      n = this.animated;
                    this.mpScrollToMethod({
                      top: t,
                      animated: n,
                    });
                  },
                },
                {
                  key: "watchScrollIntoView",
                  value: function watchScrollIntoView(e) {
                    this.mpScrollIntoViewMethod(e);
                  },
                },
                {
                  key: "handleScroll",
                  value: function handleScroll(e) {
                    var t;
                    if (!(e instanceof CustomEvent)) {
                      e.stopPropagation(),
                        null === (t = e.stopImmediatePropagation) ||
                          void 0 === t ||
                          t.call(e);
                      var n = this.el,
                        i = n.scrollLeft,
                        r = n.scrollTop,
                        a = n.scrollHeight,
                        o = n.scrollWidth;
                      (this._scrollLeft = i),
                        (this._scrollTop = r),
                        this.upperAndLower(),
                        this.onScroll.emit({
                          scrollLeft: i,
                          scrollTop: r,
                          scrollHeight: a,
                          scrollWidth: o,
                        });
                    }
                  },
                },
                {
                  key: "handleTouchMove",
                  value: function handleTouchMove(e) {
                    e instanceof CustomEvent || e.stopPropagation();
                  },
                },
                {
                  key: "mpScrollToMethod",
                  value:
                    ((n = (0, T.A)(
                      (0, C.A)().mark(function _callee(e) {
                        var t,
                          n,
                          i,
                          r,
                          a,
                          o = this;
                        return (0, C.A)().wrap(
                          function _callee$(s) {
                            for (;;)
                              switch ((s.prev = s.next)) {
                                case 0:
                                  (t = e.top),
                                    (n = e.left),
                                    (i = e.duration),
                                    (r = e.animated),
                                    (a = void 0 !== r && r),
                                    this.scrollY &&
                                      "number" == typeof t &&
                                      !isNaN(t) &&
                                      t !== this._scrollTop &&
                                      (a
                                        ? easeOutScroll(
                                            this._scrollTop,
                                            t,
                                            i,
                                            function (e) {
                                              return (o.el.scrollTop = e);
                                            }
                                          )
                                        : (this.el.scrollTop = t),
                                      (this._scrollTop = t)),
                                    this.scrollX &&
                                      "number" == typeof n &&
                                      !isNaN(n) &&
                                      n !== this._scrollLeft &&
                                      (a
                                        ? easeOutScroll(
                                            this._scrollLeft,
                                            n,
                                            i,
                                            function (e) {
                                              return (o.el.scrollLeft = e);
                                            }
                                          )
                                        : (this.el.scrollLeft = n),
                                      (this._scrollLeft = n));
                                case 3:
                                case "end":
                                  return s.stop();
                              }
                          },
                          _callee,
                          this
                        );
                      })
                    )),
                    function mpScrollToMethod(e) {
                      return n.apply(this, arguments);
                    }),
                },
                {
                  key: "mpScrollIntoViewMethod",
                  value:
                    ((t = (0, T.A)(
                      (0, C.A)().mark(function _callee2(e) {
                        var t;
                        return (0, C.A)().wrap(
                          function _callee2$(n) {
                            for (;;)
                              switch ((n.prev = n.next)) {
                                case 0:
                                  "string" == typeof e &&
                                    e &&
                                    (null ===
                                      (t = document.querySelector(
                                        "#".concat(e)
                                      )) ||
                                      void 0 === t ||
                                      t.scrollIntoView({
                                        behavior: this.animated
                                          ? "smooth"
                                          : "auto",
                                        block:
                                          (this.scrollY &&
                                            this.mpScrollIntoViewAlignment) ||
                                          "center",
                                        inline:
                                          (this.scrollX &&
                                            this.mpScrollIntoViewAlignment) ||
                                          "start",
                                      }));
                                case 1:
                                case "end":
                                  return n.stop();
                              }
                          },
                          _callee2,
                          this
                        );
                      })
                    )),
                    function mpScrollIntoViewMethod(e) {
                      return t.apply(this, arguments);
                    }),
                },
                {
                  key: "componentDidLoad",
                  value: function componentDidLoad() {
                    var e = Number(this.mpScrollTop),
                      t = Number(this.mpScrollLeft),
                      n = this.animated;
                    this.mpScrollToMethod({
                      top: e,
                      left: t,
                      animated: n,
                    });
                  },
                },
                {
                  key: "componentDidRender",
                  value: function componentDidRender() {
                    handleStencilNodes(this.el);
                  },
                },
                {
                  key: "render",
                  value: function render() {
                    var e = this.scrollX,
                      t = this.scrollY,
                      n = (0, u.c)({
                        "taro-scroll-view__scroll-x": e,
                        "taro-scroll-view__scroll-y": t,
                      });
                    return (0, d.h)(
                      d.xr,
                      {
                        class: n,
                      },
                      (0, d.h)("slot", null)
                    );
                  },
                },
                {
                  key: "el",
                  get: function get() {
                    return this;
                  },
                },
              ],
              [
                {
                  key: "watchers",
                  get: function get() {
                    return {
                      mpScrollLeft: ["watchScrollLeft"],
                      mpScrollTop: ["watchScrollTop"],
                      mpScrollIntoView: ["watchScrollIntoView"],
                    };
                  },
                },
                {
                  key: "style",
                  get: function get() {
                    return "taro-scroll-view-core{display:block;width:100%;-webkit-overflow-scrolling:auto}taro-scroll-view-core::-webkit-scrollbar{display:none}.taro-scroll-view__scroll-x{overflow-x:scroll;overflow-y:hidden}.taro-scroll-view__scroll-y{overflow-x:hidden;overflow-y:scroll}";
                  },
                },
              ]
            )
          );
          var t, n;
        })(d.wt),
        [
          4,
          "taro-scroll-view-core",
          {
            scrollX: [4, "scroll-x"],
            scrollY: [4, "scroll-y"],
            upperThreshold: [8, "upper-threshold"],
            lowerThreshold: [8, "lower-threshold"],
            mpScrollTop: [520, "scroll-top"],
            mpScrollLeft: [520, "scroll-left"],
            mpScrollIntoView: [513, "scroll-into-view"],
            mpScrollIntoViewAlignment: [1, "scroll-into-view-alignment"],
            animated: [4, "scroll-with-animation"],
            mpScrollToMethod: [64],
            mpScrollIntoViewMethod: [64],
          },
          [
            [1, "scroll", "handleScroll"],
            [1, "touchmove", "handleTouchMove"],
          ],
        ]
      );
      var B = function taro_scroll_view_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-scroll-view-core"].forEach(function (e) {
              if ("taro-scroll-view-core" === e)
                customElements.get(e) || customElements.define(e, j);
            });
          }
        },
        R = n(3802),
        F = n(4308),
        Y = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.selectable = !1),
                (e.userSelect = !1),
                (e.space = void 0),
                (e.numberOfLines = void 0),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(
                _class,
                [
                  {
                    key: "render",
                    value: function render() {
                      var e = {};
                      return (
                        "number" == typeof this.numberOfLines &&
                          (e["--line-clamp"] = this.numberOfLines),
                        (0, d.h)(
                          d.xr,
                          {
                            style: e,
                          },
                          (0, d.h)("slot", null)
                        )
                      );
                    },
                  },
                ],
                [
                  {
                    key: "style",
                    get: function get() {
                      return "taro-text-core{display:inline;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}taro-text-core[selectable=true],taro-text-core[user-select=true]{display:inline-block;-webkit-user-select:text;-moz-user-select:text;-ms-user-select:text;user-select:text}taro-text-core[space]{white-space:pre-wrap}taro-text-core[space=ensp]{word-spacing:0.5em}taro-text-core[space=nbsp]{word-spacing:1em}taro-text-core[number-of-lines]{--line-clamp:2;display:-webkit-box;-webkit-box-orient:vertical;word-wrap:break-word;overflow:hidden;text-overflow:ellipsis;-webkit-line-clamp:var(--line-clamp)}";
                    },
                  },
                ]
              )
            );
          })(d.wt),
          [
            4,
            "taro-text-core",
            {
              selectable: [1028],
              userSelect: [1028, "user-select"],
              space: [1025],
              numberOfLines: [2, "number-of-lines"],
            },
          ]
        );
      var G = function taro_text_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            ["taro-text-core"].forEach(function (e) {
              if ("taro-text-core" === e)
                customElements.get(e) || customElements.define(e, Y);
            });
          }
        },
        V = function formatTime(e) {
          if (!e) return "";
          var t = Math.round(e % 60),
            n = Math.round((e - t) / 60);
          return ""
            .concat(n < 10 ? "0".concat(n) : n, ":")
            .concat(t < 10 ? "0".concat(t) : t);
        },
        W = function normalizeNumber(e) {
          return Math.max(-1, Math.min(e, 1));
        },
        H = "default",
        X = (function () {
          for (
            var e,
              t = [
                [
                  "requestFullscreen",
                  "exitFullscreen",
                  "fullscreenElement",
                  "fullscreenEnabled",
                  "fullscreenchange",
                  "fullscreenerror",
                ],
                [
                  "webkitRequestFullscreen",
                  "webkitExitFullscreen",
                  "webkitFullscreenElement",
                  "webkitFullscreenEnabled",
                  "webkitfullscreenchange",
                  "webkitfullscreenerror",
                ],
                [
                  "webkitRequestFullScreen",
                  "webkitCancelFullScreen",
                  "webkitCurrentFullScreenElement",
                  "webkitCancelFullScreen",
                  "webkitfullscreenchange",
                  "webkitfullscreenerror",
                ],
                [
                  "mozRequestFullScreen",
                  "mozCancelFullScreen",
                  "mozFullScreenElement",
                  "mozFullScreenEnabled",
                  "mozfullscreenchange",
                  "mozfullscreenerror",
                ],
                [
                  "msRequestFullscreen",
                  "msExitFullscreen",
                  "msFullscreenElement",
                  "msFullscreenEnabled",
                  "MSFullscreenChange",
                  "MSFullscreenError",
                ],
              ],
              n = [
                "webkitEnterFullscreen",
                "webkitExitFullscreen",
                "webkitCurrentFullScreenElement",
                "webkitSupportsFullscreen",
                "fullscreenchange",
                "fullscreenerror",
              ],
              i = 0,
              r = t.length,
              a = {};
            i < r;
            i++
          )
            if ((e = t[i]) && e[1] in document) {
              for (i = 0; i < e.length; i++) a[t[0][i]] = e[i];
              return a;
            }
          if (!a[t[0][0]])
            for (H = "iOS", i = 0; i < n.length; i++) a[t[0][i]] = n[i];
          return a;
        })(),
        U = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.visible = !1),
                (e.isDraggingProgressBall = !1),
                (e.percentage = 0),
                (e.progressDimensions = {
                  left: 0,
                  width: 0,
                }),
                (e.calcPercentage = function (t) {
                  var n = t - e.progressDimensions.left;
                  return (
                    (n = Math.max(n, 0)),
                    (n = Math.min(n, e.progressDimensions.width)) /
                      e.progressDimensions.width
                  );
                }),
                (e.onDragProgressBallStart = function () {
                  (e.isDraggingProgressBall = !0),
                    e.hideControlsTimer && clearTimeout(e.hideControlsTimer);
                }),
                (e.onClickProgress = function (t) {
                  t.stopPropagation();
                  var n = e.calcPercentage(t.pageX);
                  e.seekFunc(n * e.duration), e.toggleVisibility(!0);
                }),
                (e.controls = void 0),
                (e.currentTime = void 0),
                (e.duration = void 0),
                (e.isPlaying = void 0),
                (e.pauseFunc = void 0),
                (e.playFunc = void 0),
                (e.seekFunc = void 0),
                (e.showPlayBtn = void 0),
                (e.showProgress = void 0),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(_class, [
                {
                  key: "onDocumentTouchMove",
                  value: function onDocumentTouchMove(e) {
                    if (this.isDraggingProgressBall) {
                      var t = e.touches[0].pageX;
                      (this.percentage = this.calcPercentage(t)),
                        this.setProgressBall(this.percentage),
                        this.setCurrentTime(this.percentage * this.duration);
                    }
                  },
                },
                {
                  key: "onDocumentTouchEnd",
                  value: function onDocumentTouchEnd() {
                    this.isDraggingProgressBall &&
                      ((this.isDraggingProgressBall = !1),
                      this.seekFunc(this.percentage * this.duration),
                      this.toggleVisibility(!0));
                  },
                },
                {
                  key: "setProgressBall",
                  value:
                    ((r = (0, T.A)(
                      (0, C.A)().mark(function _callee(e) {
                        return (0, C.A)().wrap(
                          function _callee$(t) {
                            for (;;)
                              switch ((t.prev = t.next)) {
                                case 0:
                                  this.progressBallRef &&
                                    (this.progressBallRef.style.left =
                                      "".concat(100 * e, "%"));
                                case 1:
                                case "end":
                                  return t.stop();
                              }
                          },
                          _callee,
                          this
                        );
                      })
                    )),
                    function setProgressBall(e) {
                      return r.apply(this, arguments);
                    }),
                },
                {
                  key: "toggleVisibility",
                  value:
                    ((i = (0, T.A)(
                      (0, C.A)().mark(function _callee2(e) {
                        var t,
                          n = this;
                        return (0, C.A)().wrap(
                          function _callee2$(i) {
                            for (;;)
                              switch ((i.prev = i.next)) {
                                case 0:
                                  (t = void 0 === e ? !this.visible : e)
                                    ? (this.hideControlsTimer &&
                                        clearTimeout(this.hideControlsTimer),
                                      this.isPlaying &&
                                        (this.hideControlsTimer = setTimeout(
                                          function () {
                                            n.toggleVisibility(!1);
                                          },
                                          2e3
                                        )),
                                      (this.el.style.visibility = "visible"))
                                    : (this.el.style.visibility = "hidden"),
                                    (this.visible = !!t);
                                case 3:
                                case "end":
                                  return i.stop();
                              }
                          },
                          _callee2,
                          this
                        );
                      })
                    )),
                    function toggleVisibility(e) {
                      return i.apply(this, arguments);
                    }),
                },
                {
                  key: "getIsDraggingProgressBall",
                  value:
                    ((n = (0, T.A)(
                      (0, C.A)().mark(function _callee3() {
                        return (0, C.A)().wrap(
                          function _callee3$(e) {
                            for (;;)
                              switch ((e.prev = e.next)) {
                                case 0:
                                  return e.abrupt(
                                    "return",
                                    this.isDraggingProgressBall
                                  );
                                case 1:
                                case "end":
                                  return e.stop();
                              }
                          },
                          _callee3,
                          this
                        );
                      })
                    )),
                    function getIsDraggingProgressBall() {
                      return n.apply(this, arguments);
                    }),
                },
                {
                  key: "setCurrentTime",
                  value:
                    ((t = (0, T.A)(
                      (0, C.A)().mark(function _callee4(e) {
                        return (0, C.A)().wrap(
                          function _callee4$(t) {
                            for (;;)
                              switch ((t.prev = t.next)) {
                                case 0:
                                  this.currentTimeRef.innerHTML = V(e);
                                case 1:
                                case "end":
                                  return t.stop();
                              }
                          },
                          _callee4,
                          this
                        );
                      })
                    )),
                    function setCurrentTime(e) {
                      return t.apply(this, arguments);
                    }),
                },
                {
                  key: "render",
                  value: function render() {
                    var e,
                      t = this,
                      n = this.controls,
                      i = this.currentTime,
                      r = this.duration,
                      a = this.isPlaying,
                      o = this.pauseFunc,
                      s = this.playFunc,
                      l = this.showPlayBtn,
                      c = this.showProgress,
                      u = V(r);
                    return (
                      (e = l
                        ? a
                          ? (0, d.h)("div", {
                              class:
                                "taro-video-control-button taro-video-control-button-pause",
                              onClick: o,
                            })
                          : (0, d.h)("div", {
                              class:
                                "taro-video-control-button taro-video-control-button-play",
                              onClick: s,
                            })
                        : null),
                      (0, d.h)(
                        d.xr,
                        {
                          class: "taro-video-bar taro-video-bar-full",
                        },
                        n &&
                          (0, d.h)(
                            "div",
                            {
                              class: "taro-video-controls",
                            },
                            e,
                            c &&
                              (0, d.h)(
                                "div",
                                {
                                  class: "taro-video-current-time",
                                  ref: function ref(e) {
                                    return (t.currentTimeRef = e);
                                  },
                                },
                                V(i)
                              ),
                            c &&
                              (0, d.h)(
                                "div",
                                {
                                  class: "taro-video-progress-container",
                                  onClick: this.onClickProgress,
                                },
                                (0, d.h)(
                                  "div",
                                  {
                                    class: "taro-video-progress",
                                    ref: function ref(e) {
                                      if (e) {
                                        var n = e.getBoundingClientRect();
                                        (t.progressDimensions.left = n.left),
                                          (t.progressDimensions.width =
                                            n.width);
                                      }
                                    },
                                  },
                                  (0, d.h)("div", {
                                    class: "taro-video-progress-buffered",
                                    style: {
                                      width: "100%",
                                    },
                                  }),
                                  (0, d.h)(
                                    "div",
                                    {
                                      class: "taro-video-ball",
                                      ref: function ref(e) {
                                        return (t.progressBallRef = e);
                                      },
                                      onTouchStart:
                                        this.onDragProgressBallStart,
                                      style: {
                                        left: "".concat(
                                          u ? (this.currentTime / r) * 100 : 0,
                                          "%"
                                        ),
                                      },
                                    },
                                    (0, d.h)("div", {
                                      class: "taro-video-inner",
                                    })
                                  )
                                )
                              ),
                            c &&
                              (0, d.h)(
                                "div",
                                {
                                  class: "taro-video-duration",
                                },
                                u
                              )
                          ),
                        (0, d.h)("slot", null)
                      )
                    );
                  },
                },
                {
                  key: "el",
                  get: function get() {
                    return this;
                  },
                },
              ])
            );
            var t, n, i, r;
          })(d.wt),
          [
            4,
            "taro-video-control",
            {
              controls: [4],
              currentTime: [2, "current-time"],
              duration: [2],
              isPlaying: [4, "is-playing"],
              pauseFunc: [16],
              playFunc: [16],
              seekFunc: [16],
              showPlayBtn: [4, "show-play-btn"],
              showProgress: [4, "show-progress"],
              setProgressBall: [64],
              toggleVisibility: [64],
              getIsDraggingProgressBall: [64],
              setCurrentTime: [64],
            },
            [
              [5, "touchmove", "onDocumentTouchMove"],
              [5, "touchend", "onDocumentTouchEnd"],
              [5, "touchcancel", "onDocumentTouchEnd"],
            ],
          ]
        );
      var Z = n(436),
        Q = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.list = []),
                (e.danmuElList = []),
                (e.currentTime = 0),
                (e.enable = !1),
                (e.danmuList = []),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(_class, [
                {
                  key: "ensureProperties",
                  value: function ensureProperties(e) {
                    var t = Object.assign({}, e);
                    return (
                      "time" in e || (t.time = this.currentTime),
                      (t.key = Math.random()),
                      (t.bottom = "".concat(90 * Math.random() + 5, "%")),
                      t
                    );
                  },
                },
                {
                  key: "sendDanmu",
                  value:
                    ((n = (0, T.A)(
                      (0, C.A)().mark(function _callee() {
                        var e,
                          t,
                          n = this,
                          i = arguments;
                        return (0, C.A)().wrap(
                          function _callee$(r) {
                            for (;;)
                              switch ((r.prev = r.next)) {
                                case 0:
                                  (e =
                                    i.length > 0 && void 0 !== i[0]
                                      ? i[0]
                                      : []),
                                    Array.isArray(e)
                                      ? (this.list = [].concat(
                                          (0, Z.A)(this.list),
                                          (0, Z.A)(
                                            e.map(function (e) {
                                              return n.ensureProperties(e);
                                            })
                                          )
                                        ))
                                      : ((t = e),
                                        (this.list = [].concat(
                                          (0, Z.A)(this.list),
                                          [
                                            Object.assign(
                                              {},
                                              this.ensureProperties(t)
                                            ),
                                          ]
                                        )));
                                case 2:
                                case "end":
                                  return r.stop();
                              }
                          },
                          _callee,
                          this
                        );
                      })
                    )),
                    function sendDanmu() {
                      return n.apply(this, arguments);
                    }),
                },
                {
                  key: "tick",
                  value:
                    ((t = (0, T.A)(
                      (0, C.A)().mark(function _callee2(e) {
                        var t, n, i;
                        return (0, C.A)().wrap(
                          function _callee2$(r) {
                            for (;;)
                              switch ((r.prev = r.next)) {
                                case 0:
                                  if (((this.currentTime = e), this.enable)) {
                                    r.next = 3;
                                    break;
                                  }
                                  return r.abrupt("return");
                                case 3:
                                  (t = this.list),
                                    (n = t.filter(function (t) {
                                      var n = t.time;
                                      return e - n < 4 && e > n;
                                    })),
                                    (i = this.danmuList),
                                    (n.length !== i.length ||
                                      n.some(function (e) {
                                        var t = e.key;
                                        return i.every(function (e) {
                                          return t !== e.key;
                                        });
                                      })) &&
                                      (this.danmuList = n);
                                case 9:
                                case "end":
                                  return r.stop();
                              }
                          },
                          _callee2,
                          this
                        );
                      })
                    )),
                    function tick(e) {
                      return t.apply(this, arguments);
                    }),
                },
                {
                  key: "componentDidUpdate",
                  value: function componentDidUpdate() {
                    var e = this;
                    requestAnimationFrame(function () {
                      setTimeout(function () {
                        e.danmuElList.splice(0).forEach(function (e) {
                          (e.style.left = "0"),
                            (e.style.webkitTransform = "translateX(-100%)"),
                            (e.style.transform = "translateX(-100%)");
                        });
                      });
                    });
                  },
                },
                {
                  key: "render",
                  value: function render() {
                    var e = this;
                    return this.enable
                      ? (0, d.h)(
                          d.xr,
                          {
                            class: "taro-video-danmu",
                          },
                          this.danmuList.map(function (t) {
                            var n = t.text,
                              i = t.color,
                              r = t.bottom,
                              a = t.key;
                            return (0, d.h)(
                              "p",
                              {
                                class: "taro-video-danmu-item",
                                key: a,
                                style: {
                                  color: i,
                                  bottom: r,
                                },
                                ref: function ref(t) {
                                  t && e.danmuElList.push(t);
                                },
                              },
                              n
                            );
                          })
                        )
                      : "";
                  },
                },
              ])
            );
            var t, n;
          })(d.wt),
          [
            0,
            "taro-video-danmu",
            {
              enable: [4],
              danmuList: [32],
              sendDanmu: [64],
              tick: [64],
            },
          ]
        );
      var q = (0, d.w$)(
        (function (e) {
          function _class() {
            var e;
            return (
              (0, o.A)(this, _class),
              (e = (0, l.A)(this, _class)).__registerHost(),
              (e.onPlay = (0, d.lh)(e, "play", 7)),
              (e.onPause = (0, d.lh)(e, "pause", 7)),
              (e.onEnded = (0, d.lh)(e, "ended", 7)),
              (e.onTimeUpdate = (0, d.lh)(e, "timeupdate", 7)),
              (e.onError = (0, d.lh)(e, "error", 7)),
              (e.onFullScreenChange = (0, d.lh)(e, "fullscreenchange", 7)),
              (e.onProgress = (0, d.lh)(e, "progress", 7)),
              (e.onLoadedMetaData = (0, d.lh)(e, "loadedmetadata", 7)),
              (e.currentTime = 0),
              (e.isDraggingProgress = !1),
              (e.gestureType = "none"),
              (e.analyzeGesture = function (t) {
                var n,
                  i = {
                    type: "none",
                  },
                  r = t.touches[0].screenX,
                  a = t.touches[0].screenY,
                  o = r - e.lastTouchScreenX,
                  s = a - e.lastTouchScreenY,
                  l = e.isFullScreen
                    ? e.vslideGestureInFullscreen
                    : e.vslideGesture;
                if ("none" === e.gestureType) {
                  if (
                    (function calcDist(e, t) {
                      return Math.sqrt(Math.pow(e, 2) + Math.pow(t, 2));
                    })(o, s) < 10
                  )
                    return i;
                  if (Math.abs(s) >= Math.abs(o)) {
                    if (!l) return i;
                    (e.gestureType = "adjustVolume"),
                      (e.lastVolume = e.videoRef.volume);
                  } else if (Math.abs(s) < Math.abs(o)) {
                    if (!e.enableProgressGesture) return i;
                    (e.gestureType = "adjustProgress"),
                      (e.lastPercentage =
                        e.currentTime /
                        (null !== (n = e.duration) && void 0 !== n
                          ? n
                          : e._duration));
                  }
                }
                return (
                  (i.type = e.gestureType),
                  (i.dataX = W(o / 200)),
                  (i.dataY = W(s / 200)),
                  i
                );
              }),
              (e.loadNativePlayer = function () {
                var t, n;
                e.videoRef &&
                  ((e.videoRef.src = e.src),
                  null === (n = (t = e.videoRef).load) ||
                    void 0 === n ||
                    n.call(t));
              }),
              (e.init = function () {
                var t = e,
                  i = t.src,
                  r = t.videoRef;
                !(function isHls(e) {
                  return /\.(m3u8)($|\?)/i.test(e);
                })(i)
                  ? e.loadNativePlayer()
                  : n
                      .e(1894)
                      .then(n.bind(n, 1894))
                      .then(function (t) {
                        var n = t.default;
                        (e.HLS = n),
                          n.isSupported()
                            ? (e.hls && e.hls.destroy(),
                              (e.hls = new n()),
                              e.hls.loadSource(i),
                              e.hls.attachMedia(r),
                              e.hls.on(n.Events.MANIFEST_PARSED, function () {
                                e.autoplay && e.play();
                              }),
                              e.hls.on(n.Events.ERROR, function (t, n) {
                                e.handleError(n);
                              }))
                            : r.canPlayType("application/vnd.apple.mpegurl")
                            ? e.loadNativePlayer()
                            : console.error("该浏览器不支持 HLS 播放");
                      });
              }),
              (e.handlePlay = function () {
                (e.isPlaying = !0),
                  (e.isFirst = !1),
                  e.controlsRef.toggleVisibility(!0),
                  e.onPlay.emit();
              }),
              (e.handlePause = function () {
                (e.isPlaying = !1),
                  e.controlsRef.toggleVisibility(!0),
                  e.onPause.emit();
              }),
              (e.handleEnded = function () {
                (e.isFirst = !0),
                  e.pause(),
                  e.controlsRef.toggleVisibility(),
                  e.onEnded.emit();
              }),
              (e.handleTimeUpdate = (0, z.t)(
                (function () {
                  var t = (0, T.A)(
                    (0, C.A)().mark(function _callee(t) {
                      var n, i, r, a;
                      return (0, C.A)().wrap(function _callee$(o) {
                        for (;;)
                          switch ((o.prev = o.next)) {
                            case 0:
                              return (
                                (e.currentTime = e.videoRef.currentTime),
                                (r = e.duration || e._duration),
                                (o.next = 4),
                                e.controlsRef.getIsDraggingProgressBall()
                              );
                            case 4:
                              (a = o.sent),
                                e.controls &&
                                  e.showProgress &&
                                  (a ||
                                    e.isDraggingProgress ||
                                    (e.controlsRef.setProgressBall(
                                      e.currentTime / r
                                    ),
                                    e.controlsRef.setCurrentTime(
                                      e.currentTime
                                    ))),
                                e.danmuRef.tick(e.currentTime),
                                e.onTimeUpdate.emit({
                                  duration:
                                    null === (n = t.target) || void 0 === n
                                      ? void 0
                                      : n.duration,
                                  currentTime:
                                    null === (i = t.target) || void 0 === i
                                      ? void 0
                                      : i.currentTime,
                                }),
                                e.duration &&
                                  e.currentTime >= e.duration &&
                                  (e.seek(0), e.handleEnded());
                            case 9:
                            case "end":
                              return o.stop();
                          }
                      }, _callee);
                    })
                  );
                  return function (e) {
                    return t.apply(this, arguments);
                  };
                })(),
                250
              )),
              (e.handleError = function (t) {
                var n, i;
                if (e.hls)
                  switch (t.type) {
                    case e.HLS.ErrorTypes.NETWORK_ERROR:
                      e.onError.emit({
                        errMsg: t.response,
                      }),
                        e.hls.startLoad();
                      break;
                    case e.HLS.ErrorTypes.MEDIA_ERROR:
                      e.onError.emit({
                        errMsg: t.reason || "媒体错误,请重试",
                      }),
                        e.hls.recoverMediaError();
                  }
                else
                  e.onError.emit({
                    errMsg:
                      null ===
                        (i =
                          null === (n = t.target) || void 0 === n
                            ? void 0
                            : n.error) || void 0 === i
                        ? void 0
                        : i.message,
                  });
              }),
              (e.handleDurationChange = function () {
                e._duration = e.videoRef.duration;
              }),
              (e.handleProgress = function () {
                e.onProgress.emit();
              }),
              (e.handleLoadedMetaData = function (t) {
                var n = t.target;
                e.onLoadedMetaData.emit({
                  width: n.videoWidth,
                  height: n.videoHeight,
                  duration: n.duration,
                });
              }),
              (e._play = function () {
                return e.videoRef.play();
              }),
              (e._pause = function () {
                return e.videoRef.pause();
              }),
              (e._stop = function () {
                e.videoRef.pause(), e._seek(0);
              }),
              (e._seek = function (t) {
                e.videoRef.currentTime = t;
              }),
              (e.onTouchStartContainer = function (t) {
                (e.lastTouchScreenX = t.touches[0].screenX),
                  (e.lastTouchScreenY = t.touches[0].screenY);
              }),
              (e.onClickContainer = function () {
                if (e.enablePlayGesture) {
                  var t = Date.now();
                  t - e.lastClickedTime < 300 &&
                    (e.isPlaying ? e.pause() : e.play()),
                    (e.lastClickedTime = t);
                }
                e.controlsRef.toggleVisibility();
              }),
              (e.onClickFullScreenBtn = function (t) {
                t.stopPropagation(), e.toggleFullScreen();
              }),
              (e.handleFullScreenChange = function (t) {
                var n = new Date().getTime();
                !t.detail &&
                  e.isFullScreen &&
                  !document[X.fullscreenElement] &&
                  n - e.fullScreenTimestamp > 100 &&
                  e.toggleFullScreen(!1, !0);
              }),
              (e.toggleFullScreen = function () {
                var t =
                    arguments.length > 0 && void 0 !== arguments[0]
                      ? arguments[0]
                      : !e.isFullScreen,
                  n =
                    arguments.length > 1 &&
                    void 0 !== arguments[1] &&
                    arguments[1];
                (e.isFullScreen = t),
                  e.controlsRef.toggleVisibility(!0),
                  (e.fullScreenTimestamp = new Date().getTime()),
                  e.onFullScreenChange.emit({
                    fullScreen: e.isFullScreen,
                    direction: "vertical",
                  }),
                  e.isFullScreen && !document[X.fullscreenElement]
                    ? setTimeout(function () {
                        e.videoRef[X.requestFullscreen]({
                          navigationUI: "auto",
                        });
                      }, 0)
                    : n || document[X.exitFullscreen]();
              }),
              (e.toggleMute = function (t) {
                t.stopPropagation(),
                  (e.videoRef.muted = !e.isMute),
                  e.controlsRef.toggleVisibility(!0),
                  (e.isMute = !e.isMute);
              }),
              (e.toggleDanmu = function (t) {
                t.stopPropagation(),
                  e.controlsRef.toggleVisibility(!0),
                  (e._enableDanmu = !e._enableDanmu);
              }),
              (e.src = void 0),
              (e.duration = void 0),
              (e.controls = !0),
              (e.autoplay = !1),
              (e.loop = !1),
              (e.muted = !1),
              (e.initialTime = 0),
              (e.poster = void 0),
              (e.objectFit = "contain"),
              (e.showProgress = !0),
              (e.showFullscreenBtn = !0),
              (e.showPlayBtn = !0),
              (e.showCenterPlayBtn = !0),
              (e.showMuteBtn = !1),
              (e.danmuList = void 0),
              (e.danmuBtn = !1),
              (e.enableDanmu = !1),
              (e.enablePlayGesture = !1),
              (e.enableProgressGesture = !0),
              (e.vslideGesture = !1),
              (e.vslideGestureInFullscreen = !0),
              (e.nativeProps = {}),
              (e._duration = void 0),
              (e._enableDanmu = !1),
              (e.isPlaying = !1),
              (e.isFirst = !0),
              (e.isFullScreen = !1),
              (e.fullScreenTimestamp = new Date().getTime()),
              (e.isMute = !1),
              e
            );
          }
          return (
            (0, c.A)(_class, e),
            (0, s.A)(
              _class,
              [
                {
                  key: "componentWillLoad",
                  value: function componentWillLoad() {
                    this._enableDanmu = this.enableDanmu;
                  },
                },
                {
                  key: "componentDidLoad",
                  value: function componentDidLoad() {
                    var e, t;
                    this.init(),
                      this.initialTime &&
                        (this.videoRef.currentTime = this.initialTime),
                      null === (t = (e = this.danmuRef).sendDanmu) ||
                        void 0 === t ||
                        t.call(e, this.danmuList),
                      document.addEventListener &&
                        document.addEventListener(
                          X.fullscreenchange,
                          this.handleFullScreenChange
                        ),
                      this.videoRef &&
                        "iOS" === H &&
                        this.videoRef.addEventListener(
                          "webkitendfullscreen",
                          this.handleFullScreenChange
                        );
                  },
                },
                {
                  key: "componentDidRender",
                  value: function componentDidRender() {},
                },
                {
                  key: "disconnectedCallback",
                  value: function disconnectedCallback() {
                    document.removeEventListener &&
                      document.removeEventListener(
                        X.fullscreenchange,
                        this.handleFullScreenChange
                      ),
                      this.videoRef &&
                        "iOS" === H &&
                        this.videoRef.removeEventListener(
                          "webkitendfullscreen",
                          this.handleFullScreenChange
                        );
                  },
                },
                {
                  key: "watchEnableDanmu",
                  value: function watchEnableDanmu(e) {
                    this._enableDanmu = e;
                  },
                },
                {
                  key: "watchSrc",
                  value: function watchSrc() {
                    this.init();
                  },
                },
                {
                  key: "onDocumentTouchMove",
                  value:
                    ((f = (0, T.A)(
                      (0, C.A)().mark(function _callee2(e) {
                        var t, n, i;
                        return (0, C.A)().wrap(
                          function _callee2$(r) {
                            for (;;)
                              switch ((r.prev = r.next)) {
                                case 0:
                                  if (
                                    void 0 !== this.lastTouchScreenX &&
                                    void 0 !== this.lastTouchScreenY
                                  ) {
                                    r.next = 2;
                                    break;
                                  }
                                  return r.abrupt("return");
                                case 2:
                                  return (
                                    (r.next = 4),
                                    this.controlsRef.getIsDraggingProgressBall()
                                  );
                                case 4:
                                  if (!r.sent) {
                                    r.next = 6;
                                    break;
                                  }
                                  return r.abrupt("return");
                                case 6:
                                  "adjustVolume" ===
                                  (t = this.analyzeGesture(e)).type
                                    ? ((this.toastVolumeRef.style.visibility =
                                        "visible"),
                                      (n = Math.max(
                                        Math.min(this.lastVolume - t.dataY, 1),
                                        0
                                      )),
                                      (this.videoRef.volume = n),
                                      (this.toastVolumeBarRef.style.width =
                                        "".concat(100 * n, "%")))
                                    : "adjustProgress" === t.type &&
                                      ((this.isDraggingProgress = !0),
                                      (this.nextPercentage = Math.max(
                                        Math.min(
                                          this.lastPercentage + (t.dataX || 0),
                                          1
                                        ),
                                        0
                                      )),
                                      this.controls &&
                                        this.showProgress &&
                                        (this.controlsRef.setProgressBall(
                                          this.nextPercentage
                                        ),
                                        this.controlsRef.toggleVisibility(!0)),
                                      (i = this.duration || this._duration),
                                      (this.toastProgressTitleRef.innerHTML = ""
                                        .concat(
                                          V(this.nextPercentage * i),
                                          " / "
                                        )
                                        .concat(V(i))),
                                      (this.toastProgressRef.style.visibility =
                                        "visible"));
                                case 8:
                                case "end":
                                  return r.stop();
                              }
                          },
                          _callee2,
                          this
                        );
                      })
                    )),
                    function onDocumentTouchMove(e) {
                      return f.apply(this, arguments);
                    }),
                },
                {
                  key: "onDocumentTouchEnd",
                  value: function onDocumentTouchEnd() {
                    var e;
                    "adjustVolume" === this.gestureType
                      ? (this.toastVolumeRef.style.visibility = "hidden")
                      : "adjustProgress" === this.gestureType &&
                        (this.toastProgressRef.style.visibility = "hidden"),
                      this.isDraggingProgress &&
                        ((this.isDraggingProgress = !1),
                        this.seek(
                          this.nextPercentage *
                            (null !== (e = this.duration) && void 0 !== e
                              ? e
                              : this._duration)
                        )),
                      (this.gestureType = "none"),
                      (this.lastTouchScreenX = void 0),
                      (this.lastTouchScreenY = void 0);
                  },
                },
                {
                  key: "getHlsObject",
                  value:
                    ((m = (0, T.A)(
                      (0, C.A)().mark(function _callee3() {
                        return (0, C.A)().wrap(
                          function _callee3$(e) {
                            for (;;)
                              switch ((e.prev = e.next)) {
                                case 0:
                                  return e.abrupt("return", this.hls);
                                case 1:
                                case "end":
                                  return e.stop();
                              }
                          },
                          _callee3,
                          this
                        );
                      })
                    )),
                    function getHlsObject() {
                      return m.apply(this, arguments);
                    }),
                },
                {
                  key: "play",
                  value:
                    ((h = (0, T.A)(
                      (0, C.A)().mark(function _callee4() {
                        return (0, C.A)().wrap(
                          function _callee4$(e) {
                            for (;;)
                              switch ((e.prev = e.next)) {
                                case 0:
                                  this._play();
                                case 1:
                                case "end":
                                  return e.stop();
                              }
                          },
                          _callee4,
                          this
                        );
                      })
                    )),
                    function play() {
                      return h.apply(this, arguments);
                    }),
                },
                {
                  key: "pause",
                  value:
                    ((p = (0, T.A)(
                      (0, C.A)().mark(function _callee5() {
                        return (0, C.A)().wrap(
                          function _callee5$(e) {
                            for (;;)
                              switch ((e.prev = e.next)) {
                                case 0:
                                  this._pause();
                                case 1:
                                case "end":
                                  return e.stop();
                              }
                          },
                          _callee5,
                          this
                        );
                      })
                    )),
                    function pause() {
                      return p.apply(this, arguments);
                    }),
                },
                {
                  key: "stop",
                  value:
                    ((a = (0, T.A)(
                      (0, C.A)().mark(function _callee6() {
                        return (0, C.A)().wrap(
                          function _callee6$(e) {
                            for (;;)
                              switch ((e.prev = e.next)) {
                                case 0:
                                  this._stop();
                                case 1:
                                case "end":
                                  return e.stop();
                              }
                          },
                          _callee6,
                          this
                        );
                      })
                    )),
                    function stop() {
                      return a.apply(this, arguments);
                    }),
                },
                {
                  key: "seek",
                  value:
                    ((r = (0, T.A)(
                      (0, C.A)().mark(function _callee7(e) {
                        return (0, C.A)().wrap(
                          function _callee7$(t) {
                            for (;;)
                              switch ((t.prev = t.next)) {
                                case 0:
                                  this._seek(e);
                                case 1:
                                case "end":
                                  return t.stop();
                              }
                          },
                          _callee7,
                          this
                        );
                      })
                    )),
                    function seek(e) {
                      return r.apply(this, arguments);
                    }),
                },
                {
                  key: "requestFullScreen",
                  value:
                    ((i = (0, T.A)(
                      (0, C.A)().mark(function _callee8() {
                        return (0, C.A)().wrap(
                          function _callee8$(e) {
                            for (;;)
                              switch ((e.prev = e.next)) {
                                case 0:
                                  this.toggleFullScreen(!0);
                                case 1:
                                case "end":
                                  return e.stop();
                              }
                          },
                          _callee8,
                          this
                        );
                      })
                    )),
                    function requestFullScreen() {
                      return i.apply(this, arguments);
                    }),
                },
                {
                  key: "exitFullScreen",
                  value:
                    ((t = (0, T.A)(
                      (0, C.A)().mark(function _callee9() {
                        return (0, C.A)().wrap(
                          function _callee9$(e) {
                            for (;;)
                              switch ((e.prev = e.next)) {
                                case 0:
                                  this.toggleFullScreen(!1);
                                case 1:
                                case "end":
                                  return e.stop();
                              }
                          },
                          _callee9,
                          this
                        );
                      })
                    )),
                    function exitFullScreen() {
                      return t.apply(this, arguments);
                    }),
                },
                {
                  key: "render",
                  value: function render() {
                    var e = this,
                      t = this.controls,
                      n = this.autoplay,
                      i = this.loop,
                      r = this.muted,
                      a = this.poster,
                      o = this.objectFit,
                      s = this.isFirst,
                      l = this.isMute,
                      c = this.isFullScreen,
                      p = this.showCenterPlayBtn,
                      h = this.isPlaying,
                      m = this._enableDanmu,
                      f = this.showMuteBtn,
                      v = this.danmuBtn,
                      g = this.showFullscreenBtn,
                      w = this.nativeProps,
                      b = this.duration || this._duration,
                      y = V(b);
                    return (0, d.h)(
                      d.xr,
                      {
                        class: (0, u.c)("taro-video-container", {
                          "taro-video-type-fullscreen": c,
                        }),
                        onTouchStart: this.onTouchStartContainer,
                        onClick: this.onClickContainer,
                      },
                      (0, d.h)(
                        "video",
                        Object.assign(
                          {
                            class: "taro-video-video",
                            style: {
                              "object-fit": o,
                            },
                            ref: function ref(t) {
                              t && (e.videoRef = t);
                            },
                            autoplay: n,
                            loop: i,
                            muted: r,
                            poster: t ? a : void 0,
                            playsinline: !0,
                            "webkit-playsinline": !0,
                            onPlay: this.handlePlay,
                            onPause: this.handlePause,
                            onEnded: this.handleEnded,
                            onTimeUpdate: this.handleTimeUpdate,
                            onError: this.handleError,
                            onDurationChange: this.handleDurationChange,
                            onProgress: this.handleProgress,
                            onLoadedMetaData: this.handleLoadedMetaData,
                          },
                          w
                        ),
                        "暂时不支持播放该视频"
                      ),
                      (0, d.h)("taro-video-danmu", {
                        ref: function ref(t) {
                          t && (e.danmuRef = t);
                        },
                        enable: m,
                      }),
                      s &&
                        p &&
                        !h &&
                        (0, d.h)(
                          "div",
                          {
                            class: "taro-video-cover",
                          },
                          (0, d.h)("div", {
                            class: "taro-video-cover-play-button",
                            onClick: function onClick() {
                              return e.play();
                            },
                          }),
                          (0, d.h)(
                            "p",
                            {
                              class: "taro-video-cover-duration",
                            },
                            y
                          )
                        ),
                      (0, d.h)(
                        "taro-video-control",
                        {
                          ref: function ref(t) {
                            t && (e.controlsRef = t);
                          },
                          controls: t,
                          currentTime: this.currentTime,
                          duration: b,
                          isPlaying: this.isPlaying,
                          pauseFunc: this._pause,
                          playFunc: this._play,
                          seekFunc: this._seek,
                          showPlayBtn: this.showPlayBtn,
                          showProgress: this.showProgress,
                        },
                        f &&
                          (0, d.h)("div", {
                            class: (0, u.c)("taro-video-mute", {
                              "taro-video-type-mute": l,
                            }),
                            onClick: this.toggleMute,
                          }),
                        v &&
                          (0, d.h)(
                            "div",
                            {
                              class: (0, u.c)("taro-video-danmu-button", {
                                "taro-video-danmu-button-active": m,
                              }),
                              onClick: this.toggleDanmu,
                            },
                            "弹幕"
                          ),
                        g &&
                          (0, d.h)("div", {
                            class: (0, u.c)("taro-video-fullscreen", {
                              "taro-video-type-fullscreen": c,
                            }),
                            onClick: this.onClickFullScreenBtn,
                          })
                      ),
                      (0, d.h)(
                        "div",
                        {
                          class: "taro-video-toast taro-video-toast-volume",
                          ref: function ref(t) {
                            t && (e.toastVolumeRef = t);
                          },
                        },
                        (0, d.h)(
                          "div",
                          {
                            class: "taro-video-toast-title",
                          },
                          "音量"
                        ),
                        (0, d.h)("div", {
                          class: "taro-video-toast-icon",
                        }),
                        (0, d.h)(
                          "div",
                          {
                            class: "taro-video-toast-value",
                          },
                          (0, d.h)(
                            "div",
                            {
                              class: "taro-video-toast-value-content",
                              ref: function ref(t) {
                                t && (e.toastVolumeBarRef = t);
                              },
                            },
                            (0, d.h)(
                              "div",
                              {
                                class: "taro-video-toast-volume-grids",
                              },
                              Array(10)
                                .fill(1)
                                .map(function () {
                                  return (0, d.h)("div", {
                                    class: "taro-video-toast-volume-grids-item",
                                  });
                                })
                            )
                          )
                        )
                      ),
                      (0, d.h)(
                        "div",
                        {
                          class: "taro-video-toast taro-video-toast-progress",
                          ref: function ref(t) {
                            t && (e.toastProgressRef = t);
                          },
                        },
                        (0, d.h)("div", {
                          class: "taro-video-toast-title",
                          ref: function ref(t) {
                            t && (e.toastProgressTitleRef = t);
                          },
                        })
                      )
                    );
                  },
                },
                {
                  key: "el",
                  get: function get() {
                    return this;
                  },
                },
              ],
              [
                {
                  key: "watchers",
                  get: function get() {
                    return {
                      enableDanmu: ["watchEnableDanmu"],
                      src: ["watchSrc"],
                    };
                  },
                },
                {
                  key: "style",
                  get: function get() {
                    return '.taro-video{display:inline-block;overflow:hidden;position:relative;width:100%;height:225px;line-height:0}.taro-video[hidden]{display:none}.taro-video-container{display:inline-block;position:absolute;left:0;top:0;width:100%;height:100%;background-color:#000;-o-object-position:inherit;object-position:inherit}.taro-video-container.taro-video-type-fullscreen{position:fixed;left:0;right:0;top:0;bottom:0;z-index:999}.taro-video-container.taro-video-type-fullscreen.taro-video-type-rotate-left{-webkit-transform:translate(-50%, -50%) rotate(-90deg);transform:translate(-50%, -50%) rotate(-90deg)}.taro-video-container.taro-video-type-fullscreen.taro-video-type-rotate-right{-webkit-transform:translate(-50%, -50%) rotate(90deg);transform:translate(-50%, -50%) rotate(90deg)}.taro-video-video{width:100%;height:100%;-o-object-position:inherit;object-position:inherit;display:block}.taro-video-cover{display:-ms-flexbox;display:flex;position:absolute;left:0;top:0;bottom:0;z-index:1;-ms-flex-direction:column;flex-direction:column;-ms-flex-pack:center;justify-content:center;-ms-flex-align:center;align-items:center;width:100%;background-color:rgba(1, 1, 1, 0.5);-webkit-box-orient:vertical;-webkit-box-direction:normal;-webkit-box-pack:center;-webkit-box-align:center}.taro-video-cover-play-button{width:40px;height:40px;background-repeat:no-repeat;background-position:50% 50%;background-size:50%}.taro-video-cover-duration{margin-top:10px;line-height:1;font-size:16px;color:#fff}.taro-video-bar{display:-ms-flexbox;display:flex;visibility:hidden;overflow:hidden;position:absolute;right:0;bottom:0;z-index:1;-ms-flex-align:center;align-items:center;padding:0 10px;height:44px;background-color:rgba(0, 0, 0, 0.5);-webkit-box-align:center}.taro-video-bar.taro-video-bar-full{left:0}.taro-video-controls{display:-ms-flexbox;display:flex;-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;margin:0 8.5px}.taro-video-control-button{-webkit-box-sizing:content-box;box-sizing:content-box;margin-left:-8.5px;padding:14.5px 12.5px;width:13px;height:15px}.taro-video-control-button::after{display:block;width:100%;height:100%;background-repeat:no-repeat;background-position:50% 50%;background-size:100%;content:""}.taro-video-control-button.taro-video-control-button-play::after,.taro-video-cover-play-button{background-image:url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABoAAAAeCAYAAAAy2w7YAAAAAXNSR0IArs4c6QAAAWhJREFUSA1j+P///0cgBoHjQGzCQCsAtgJB/AMy5wCxGNXtQ9iBwvoA5BUCMQvVLEQxHpNzDSjkRhXLMM3GKrIeKKpEkYVYjcUu+AMo3ALE3GRZiN1MvKKPgbIRJFuG10j8koeA0gZEW4jfLIKyf4EqpgOxMEELCRpFnIJ3QGU5QMyM00LizCFa1SWgSkeslhFtBGkKVwGVy6FYSJp+klR/A6quB2JOkIWMIK0oNlOf8xBoZDE9LAI7nYn6HsBq4l96WHQEaLUpAyiOaASeAM2NgvuPBpaACt82IEYtfKls0UagecpwXyAzqGTRdaA57sjmYrAptAjUsCkGYlYMg9EFyLQI1IiZB8Ti6Obh5JNh0QmgHlOcBuKSIMGi50C18UDMiMssvOJEWPQLqKYbiHnxGkRIkoBF24DyaoTMIEoeh0W3geI+RBlArCI0iz4D+RVAzEasfqLVAQ19AcSg5LoYiKWI1kiiQgCMBLnEEcfDSgAAAABJRU5ErkJggg==")}.taro-video-control-button.taro-video-control-button-pause::after{background-image:url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABIAAAAgCAYAAAAffCjxAAAAAXNSR0IArs4c6QAAAFlJREFUSA3tksEKACAIQ7X//5zq98wOgQayum8QaGweHhMzG/6OujzKAymn+0LMqivu1XznWmX8/echTIyMyAgTwA72iIwwAexgj8gIE8CO3aMRbDPMaEy5BRGaKcZv8YxRAAAAAElFTkSuQmCC")}.taro-video-current-time,.taro-video-duration{margin-top:15px;margin-bottom:14.5px;height:14.5px;line-height:14.5px;font-size:12px;color:#cbcbcb}.taro-video-progress-container{position:relative;-ms-flex-positive:2;flex-grow:2;-webkit-box-flex:2}.taro-video-progress{position:relative;margin:21px 12px;height:2px;background-color:rgba(255, 255, 255, 0.4)}.taro-video-progress-buffered{position:absolute;left:0;top:0;width:0;height:100%;background-color:rgba(255, 255, 255, 0.8);-webkit-transition:width 0.1s;transition:width 0.1s}.taro-video-ball{position:absolute;left:0;top:-21px;-webkit-box-sizing:content-box;box-sizing:content-box;margin-left:-22px;padding:14px;width:16px;height:16px}.taro-video-inner{border-radius:50%;width:100%;height:100%;background-color:#fff}.taro-video-danmu-button{margin:0 8.5px;padding:2px 10px;border:1px solid #fff;border-radius:5px;line-height:1;font-size:13px;color:#fff;white-space:nowrap}.taro-video-danmu-button.taro-video-danmu-button-active{border-color:#48c23d;color:#48c23d}.taro-video-fullscreen,.taro-video-mute{-webkit-box-sizing:content-box;box-sizing:content-box;padding:8.5px;width:17px;height:17px;background-repeat:no-repeat;background-position:50% 50%;background-size:50%}.taro-video-fullscreen{background-image:url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAhUlEQVRYR+2WSwrAMAhEnZO3PfmULLooGEFTiIXJ2s/kRY2wzQeb85sE9CRA8jSzY1YfAFzhJBnU1AVgxH2dSiArCnD9QgGzRNnOech48SRABHoSyFb5in3PSbhyo6yvCPQkEM3u7BsPe/0FIvBfAh/vhKmVbO9SWun1qk/PSVi9TcVPBG6R1YIhgWwNpQAAAABJRU5ErkJggg==")}.taro-video-fullscreen.taro-video-type-fullscreen{background-image:url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAABPUlEQVRYR+2Xu0pDURBF1/ZLxNcHKNiIlfhA7C0UBSEE8RNEBNFPUEQEEbGxFiSSSrCwEHsf5E/ccsSUuWfUhKQ40947+y42Z8+ZK/pcinzf9hhwD1xJ2q/qsb0JHAOzkl5y+lGAGnCWICQtZgAS6DxQk3TeLYA6cAo0JSXxjmW7CcwBdUkJurKiDhSA4kBvHbA9CqwBQx2O7BSw8ssU3ALPFRF4knT3nQLbr8B4LjLBOdAAFgJaLUkjbYC9n+zm+i4kXWbmwCqwnRMCHiXthuZAQOzPrxSA4kBxYDAcsH0EzATCfCLpJjOINoCtgFZabg7bk7AFDAeaGpKWgitZTu5N0kQbYBmYrujo9mX0CVxL+gidAdu9vY5zXhWA4sAgOND3X7NJ4AHYCaxkB8B62gslvecSFpoDOZH/PP8Cnt7hIaM5xCEAAAAASUVORK5CYII=")}.taro-video-mute{background-image:url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAGAGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDAgNzkuMTYwNDUxLCAyMDE3LzA1LzA2LTAxOjA4OjIxICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoTWFjaW50b3NoKSIgeG1wOkNyZWF0ZURhdGU9IjIwMTktMDQtMTFUMTA6MTg6MjArMDg6MDAiIHhtcDpNb2RpZnlEYXRlPSIyMDE5LTA0LTExVDEwOjIyOjIyKzA4OjAwIiB4bXA6TWV0YWRhdGFEYXRlPSIyMDE5LTA0LTExVDEwOjIyOjIyKzA4OjAwIiBkYzpmb3JtYXQ9ImltYWdlL3BuZyIgcGhvdG9zaG9wOkNvbG9yTW9kZT0iMyIgcGhvdG9zaG9wOklDQ1Byb2ZpbGU9InNSR0IgSUVDNjE5NjYtMi4xIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjk3YmE4Yjg0LTFhNTYtNGM1MS04NDVkLTNiZmYyMGI0ZDc0ZiIgeG1wTU06RG9jdW1lbnRJRD0iYWRvYmU6ZG9jaWQ6cGhvdG9zaG9wOjg1NGQ3MjlkLWUwNjctZjU0OC1hMTlhLTBlZjQ4OGRkYjJiOSIgeG1wTU06T3JpZ2luYWxEb2N1bWVudElEPSJ4bXAuZGlkOjA1ODY3ZDFlLWQ3NGEtNDgyNC04MDU3LTYzYmRmMTdjODk5ZSI+IDx4bXBNTTpIaXN0b3J5PiA8cmRmOlNlcT4gPHJkZjpsaSBzdEV2dDphY3Rpb249ImNyZWF0ZWQiIHN0RXZ0Omluc3RhbmNlSUQ9InhtcC5paWQ6MDU4NjdkMWUtZDc0YS00ODI0LTgwNTctNjNiZGYxN2M4OTllIiBzdEV2dDp3aGVuPSIyMDE5LTA0LTExVDEwOjE4OjIwKzA4OjAwIiBzdEV2dDpzb2Z0d2FyZUFnZW50PSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoTWFjaW50b3NoKSIvPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0ic2F2ZWQiIHN0RXZ0Omluc3RhbmNlSUQ9InhtcC5paWQ6OTdiYThiODQtMWE1Ni00YzUxLTg0NWQtM2JmZjIwYjRkNzRmIiBzdEV2dDp3aGVuPSIyMDE5LTA0LTExVDEwOjIyOjIyKzA4OjAwIiBzdEV2dDpzb2Z0d2FyZUFnZW50PSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoTWFjaW50b3NoKSIgc3RFdnQ6Y2hhbmdlZD0iLyIvPiA8L3JkZjpTZXE+IDwveG1wTU06SGlzdG9yeT4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz459+FoAAABqElEQVRYhc2XPWsVQRSGnxPjF4oGRfxoRQKGWCU2Ft7CykrQWosEyf/If0hhIPgHDEmbNJZqCFxiQEgTUGxsBUVEHgvnyrjZZJO92V1fGIaZnTPvszPszNlQ6VIjnbr/DwCoDLMNak/dUVfUK0f2rQugnlcX/FevWgFQH6gf3autRgHUC+piiXHzAOmtPx9gXgug8itQx9SXwDpw47AGKXZWvXvQmNFCwE3gCXA2dY0Az4GrRzHONA9cU/vAbERsllEOyh31e8USV2mrMPdG9uyn+rDom2/BHHCm5puWKiKmgdtAnz+rvaxO5mNygEvHaZ5BfADuARvAaWBpP4DGFBHfgBngFzClTrUKkCDeA+9S837rAEnbqb7VFcCpVJ/oCmCw959aB1AfAROpudYqgDoOLKRmPyLelAF8bcD4pPoMeAtcB34AT4uDBqXXwFG8XXUU/72MIuK1OgE8Bs6l7mEvo8up7lN1Ge0n9aK6VHMFZvJTr9S3CiALaCQhqZOSvegMIAvu2UVSWpigLC1fbQ0gm6in7qpfLCQbhwGIYcyPQ53/G3YO8BtUtd35bvKcVwAAAABJRU5ErkJggg==")}.taro-video-mute.taro-video-type-mute{background-image:url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACIAAAAgCAYAAAB3j6rJAAAACXBIWXMAAAsTAAALEwEAmpwYAAAGAGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDAgNzkuMTYwNDUxLCAyMDE3LzA1LzA2LTAxOjA4OjIxICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoTWFjaW50b3NoKSIgeG1wOkNyZWF0ZURhdGU9IjIwMTktMDQtMTFUMTA6MTk6MDMrMDg6MDAiIHhtcDpNb2RpZnlEYXRlPSIyMDE5LTA0LTExVDEwOjIyOjMzKzA4OjAwIiB4bXA6TWV0YWRhdGFEYXRlPSIyMDE5LTA0LTExVDEwOjIyOjMzKzA4OjAwIiBkYzpmb3JtYXQ9ImltYWdlL3BuZyIgcGhvdG9zaG9wOkNvbG9yTW9kZT0iMyIgcGhvdG9zaG9wOklDQ1Byb2ZpbGU9InNSR0IgSUVDNjE5NjYtMi4xIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjAzYjJmNjE2LTZmZTUtNDJjNC1iNTgwLTczNzZjZjI2NzdmNSIgeG1wTU06RG9jdW1lbnRJRD0iYWRvYmU6ZG9jaWQ6cGhvdG9zaG9wOjYzZjQ2NTYzLWE0ZjktOGQ0Mi1hM2FhLTY3ODJhNDBhYWNjMSIgeG1wTU06T3JpZ2luYWxEb2N1bWVudElEPSJ4bXAuZGlkOjIyYWNjMWFlLTg4ZmMtNDBlZi1iMWM1LTNmODgwY2QzYWI2MiI+IDx4bXBNTTpIaXN0b3J5PiA8cmRmOlNlcT4gPHJkZjpsaSBzdEV2dDphY3Rpb249ImNyZWF0ZWQiIHN0RXZ0Omluc3RhbmNlSUQ9InhtcC5paWQ6MjJhY2MxYWUtODhmYy00MGVmLWIxYzUtM2Y4ODBjZDNhYjYyIiBzdEV2dDp3aGVuPSIyMDE5LTA0LTExVDEwOjE5OjAzKzA4OjAwIiBzdEV2dDpzb2Z0d2FyZUFnZW50PSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoTWFjaW50b3NoKSIvPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0ic2F2ZWQiIHN0RXZ0Omluc3RhbmNlSUQ9InhtcC5paWQ6MDNiMmY2MTYtNmZlNS00MmM0LWI1ODAtNzM3NmNmMjY3N2Y1IiBzdEV2dDp3aGVuPSIyMDE5LTA0LTExVDEwOjIyOjMzKzA4OjAwIiBzdEV2dDpzb2Z0d2FyZUFnZW50PSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoTWFjaW50b3NoKSIgc3RFdnQ6Y2hhbmdlZD0iLyIvPiA8L3JkZjpTZXE+IDwveG1wTU06SGlzdG9yeT4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz5PmxYVAAACLklEQVRYhc2XP2sVQRRHz40hKoqaQgVBCy1EozFlGiVFxMLGh4piYWEh+hkEP4YKAVFEEFTyughaCH6DqIVpAhYWEgIxoJE8cywyi5tNHu/tJmvyg2WZO3dmzt47/zZUtoJ6Nhsg09YDiYhKDzACTAFNYH9lEpUq80TdrT5wpV5n/ZV9KoGoo+pXV2uyKkipOaLuUceAt8DhUvQd1FsCYhR4ChzaSIBMHSOi7lOfsByFWiCgEBH1GHAF2JlMPcBt4GC3HUYEaj9wF3gVEVPtfNVTwAXgWX7CDKq/1piAZTSZBmim8qJ6sQ3EgDqb/L7kU3MH2NHtl3dQX3r3Ak21UYAYAj4A/cl0JB+RF+uMRj4iQ+p8zt7KYFLdXKHuRi0gacBhV6a6pd5bA6KRNagFJPU9qv5u47toLmW1HnoR8Q5oAK1CVQu4FBHj/wUkaXsb+4pzpVaQFPqXrN7Be4Fx9VztIOr1BLEtmX4A94E/qdwHTKjDWYM6lu81dSlnn3V570BtuLxaMs2rZ/IgYxsBovaoPwsQA4VoFWEm8ql5DiysNyURsQTMpOIMcDYiPhd8xoGr/FtNC2G6FKXD6ihwGdiVHMoeeh8jYlA9ANwE3kTEp3bO6vE03qOONzR1r/q4RGrquaFFxFxE3ALOA9+6jExpdb180y55AhirhaRTatq0GXEzL8+ZIuI9cBJ4WKiartJf9nWV/mty7UfUafW7erpqRGI9EBuprffvu9n6C1KOmsqwI5A1AAAAAElFTkSuQmCC")}.taro-video-danmu{overflow:visible;position:absolute;left:0;top:0;bottom:0;margin-top:14px;margin-bottom:44px;width:100%;line-height:14px;font-size:14px}.taro-video-danmu-item{position:absolute;left:100%;line-height:1;color:#fff;white-space:nowrap;-webkit-transform:translateX(0);transform:translateX(0);-webkit-transition-property:left, -webkit-transform;transition-property:left, -webkit-transform;transition-property:left, transform;transition-property:left, transform, -webkit-transform;-webkit-transition-duration:3s;transition-duration:3s;-webkit-transition-timing-function:linear;transition-timing-function:linear}.taro-video-toast{display:block;visibility:hidden;position:absolute;left:50%;top:50%;border-radius:5px;background-color:rgba(255, 255, 255, 0.8);pointer-events:none;color:#000;-webkit-transform:translate(-50%, -50%);transform:translate(-50%, -50%)}.taro-video-toast.taro-video-toast-volume{display:block;width:100px;height:100px}.taro-video-toast-volume .taro-video-toast-title{display:block;margin-top:10px;width:100%;line-height:16px;text-align:center;font-size:12px}.taro-video-toast-volume .taro-video-toast-icon{display:block;margin-left:25%;width:50%;height:50%;background-image:url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAFhklEQVR4Xu2aeaxfQxTHP1VBES0NIQitWtpaaxeCUkQtaYVa0tiClAq1ExIiQTVppaWxt8RWSa2tWkJQRGgtQaSIpUKEpG0ssbbk28yV2+mZO/e9e3vvu/e98897mZnfzPl+75mZs0wvurn06ub46SGgxwLqZaA3sB/wO/A+8G/V6tS5BU4BJgJbO9DvAMOB36okoQ4CNgAeBEYbQK8Bbm4zAdsBc4EdAyA/APZoKwHHAA8DG2UA/AnYrG0ErAVcD1yXA9gfQJ8c40obsqbPgH7AY8CROTVuFQFD3X7fJid4DWsNAWOAGZ0w58YTsDZwKzChA189PbTRBGwKzAYO6iT4srbAMHfTfAksjulS1iG4JzAH2Dy2YKS/qAVcCdzi1vgZOBZ4PWvNGAG6wgYCW0IwctzNmf06BcEXtYABwOeA4otEfgEOdnGGqV6IAAUopwM6zDYuAVjeKYpYwCjgCWOhH513+b2lhE+Avrj89NPyalzyuCIE6EN9BfQ1dHoPOAD40+9LE6D/5aoqSqtLsgiQC60DTiHzfGCFoaSsQAexZdlTgEuyCDgLuK8u5G7dEAHyKN8CBrtxLwDHAX8Z+l4N3GS0L3db4aN0X5opmc+2XZQAfb2LPd2eciG1lUSRJZ9qYHkZONwiYAjwSc3gs24BXWWWfzEVuMjQez13HljX8v7A28lvEgsInaBVcxLaAvr6sgJLtBWeNTrOB+4w2p8DRvoEnAPcXTVaY70QAbrbXwIONX7zBSAL/tvrk1+iviTllnRry2irr/QSEws4D7izCxMg1XQLLAQGGXpeCkw22kOWI49RMUujCJC+uwAfGtfcImAng4AtAMsBeg04pIkESOfbgQsMsNoGnxrtSrfv7rXrSlwXWN6kLZBgEBiB8iWUUZZPIN/AF1nMoiYSICDfAlt5iB4CxhpAzwXuMtqPBuY1lYB5wFEeqFeAwwyguvIUqvuiQO/xphJwP3Cmhyh0EKrOoGDIF7n+M5pKwEwXrqdBCaQSM77Ig7SSIicDs5pKwIvACA+pzFwZIF9OlKm36QyQV7jEqDDJk5VD58uFgGIGX1Se+6yJFqAKsqI6X84GdDb4Mh0Y5zUqjFYFakUTCXja5QLSmBQHbAL8ahAgT1AeYVreSKLLphGwF/CuAfLJQLldGSTFD75c5d4mNCoWyAqG9gYWGED1AOMKo12ZbiWA/idA++deY2DVTZ0Jh5UJPsFQtD/wDaAHGWl5EzgwaUi2gK4UXS11S0cTIjrMFBtYQdAk4DID0BnAAz4B6wNLgTKKG0VIDBHwqitwpOcW+OOB540FdeipNKbUWFrUtn06o5xOij4KyDuqU0IE3Obl/rLA63pTzm9XA8jKACjdniZAaSKFmUpB1yUhAlTsUEpMh913wEkuTe7rKTzPAHqO44vpKPkFhH3cWWBVV6ogJVYZUgz/tXtIYelzI3Ct0fExIGx6j7iKWBUUveyYBehv1RIjIEsfqziq8Xp4pSBJOYTVJFQcVY3wCFeKUjVGyYfQWB00+5bEVBECrNS+qsOKBpVHNCVWHs+LS7H5PV5pOu9v0+OKEOAXR39w1e1C7wM6AkJ1eLmkRcrpRQiQrqobXO5S3vL3/4kBKMsCknV0k+iasVLUMV3UX5SAPGtED8EOT+L9YENnCasUIXNO2goChFWHqAIRyxXN4qI1BCQg9dJESYq8LnbrCBAR8t50Lig6i0krCRBoVWhVlt45wkBrCRBuRZyPuAguxIPe9lXqhpd9DcZMXOvdkPF0Xu/8dohNUmZ/1QQkuitXr+d4fryuFx3jywQYm6suAqSX8vLTXKJDt4QqO6rtLYspXWZ/nQQkOJTAUJZGIav19q9MvKvN1RUIWKMAY5P3EBBjqO393d4C/gMVHwRQlpx21QAAAABJRU5ErkJggg==");background-repeat:no-repeat;background-position:50% 50%;background-size:50%;fill:#000}.taro-video-toast-volume .taro-video-toast-value{margin-left:10px;margin-top:5px;width:80px;height:5px}.taro-video-toast-volume .taro-video-toast-value>.taro-video-toast-value-content{overflow:hidden}.taro-video-toast-volume-grids{width:80px;height:5px}.taro-video-toast-volume-grids-item{float:left;width:7.1px;height:5px;background-color:#000}.taro-video-toast-volume-grids-item:not(:first-child){margin-left:1px}.taro-video-toast.taro-video-toast-progress{padding:6px;background-color:rgba(0, 0, 0, 0.8);line-height:18px;font-size:14px;color:#fff}';
                  },
                },
              ]
            )
          );
          var t, i, r, a, p, h, m, f;
        })(d.wt),
        [
          0,
          "taro-video-core",
          {
            src: [1],
            duration: [2],
            controls: [4],
            autoplay: [4],
            loop: [4],
            muted: [4],
            initialTime: [2, "initial-time"],
            poster: [1],
            objectFit: [1, "object-fit"],
            showProgress: [4, "show-progress"],
            showFullscreenBtn: [4, "show-fullscreen-btn"],
            showPlayBtn: [4, "show-play-btn"],
            showCenterPlayBtn: [4, "show-center-play-btn"],
            showMuteBtn: [4, "show-mute-btn"],
            danmuList: [16],
            danmuBtn: [4, "danmu-btn"],
            enableDanmu: [4, "enable-danmu"],
            enablePlayGesture: [4, "enable-play-gesture"],
            enableProgressGesture: [4, "enable-progress-gesture"],
            vslideGesture: [4, "vslide-gesture"],
            vslideGestureInFullscreen: [4, "vslide-gesture-in-fullscreen"],
            nativeProps: [16],
            _duration: [32],
            _enableDanmu: [32],
            isPlaying: [32],
            isFirst: [32],
            isFullScreen: [32],
            fullScreenTimestamp: [32],
            isMute: [32],
            getHlsObject: [64],
            play: [64],
            pause: [64],
            stop: [64],
            seek: [64],
            requestFullScreen: [64],
            exitFullScreen: [64],
          },
          [
            [5, "touchmove", "onDocumentTouchMove"],
            [5, "touchend", "onDocumentTouchEnd"],
            [5, "touchcancel", "onDocumentTouchEnd"],
          ],
        ]
      );
      var J = function taro_video_core_defineCustomElement$1() {
          if ("undefined" != typeof customElements) {
            [
              "taro-video-core",
              "taro-video-control",
              "taro-video-danmu",
            ].forEach(function (e) {
              switch (e) {
                case "taro-video-core":
                  customElements.get(e) || customElements.define(e, q);
                  break;
                case "taro-video-control":
                  customElements.get(e) ||
                    (function video_control_defineCustomElement() {
                      "undefined" != typeof customElements &&
                        ["taro-video-control"].forEach(function (e) {
                          "taro-video-control" === e &&
                            (customElements.get(e) ||
                              customElements.define(e, U));
                        });
                    })();
                  break;
                case "taro-video-danmu":
                  customElements.get(e) ||
                    (function video_danmu_defineCustomElement() {
                      "undefined" != typeof customElements &&
                        ["taro-video-danmu"].forEach(function (e) {
                          "taro-video-danmu" === e &&
                            (customElements.get(e) ||
                              customElements.define(e, Q));
                        });
                    })();
              }
            });
          }
        },
        K = (0, d.w$)(
          (function (e) {
            function _class() {
              var e;
              return (
                (0, o.A)(this, _class),
                (e = (0, l.A)(this, _class)).__registerHost(),
                (e.onLongPress = (0, d.lh)(e, "longpress", 7)),
                (e.startTime = 0),
                (e.animation = void 0),
                (e.hoverClass = void 0),
                (e.hoverStartTime = 50),
                (e.hoverStayTime = 400),
                (e.hover = !1),
                (e.touch = !1),
                e
              );
            }
            return (
              (0, c.A)(_class, e),
              (0, s.A)(
                _class,
                [
                  {
                    key: "onTouchStart",
                    value: function onTouchStart() {
                      var e = this;
                      this.hoverClass &&
                        ((this.touch = !0),
                        setTimeout(function () {
                          e.touch && (e.hover = !0);
                        }, this.hoverStartTime)),
                        (this.timeoutEvent = setTimeout(function () {
                          e.onLongPress.emit();
                        }, 350)),
                        (this.startTime = Date.now());
                    },
                  },
                  {
                    key: "onTouchMove",
                    value: function onTouchMove() {
                      clearTimeout(this.timeoutEvent);
                    },
                  },
                  {
                    key: "onTouchEnd",
                    value: function onTouchEnd() {
                      var e = this;
                      Date.now() - this.startTime < 350 &&
                        clearTimeout(this.timeoutEvent),
                        this.hoverClass &&
                          ((this.touch = !1),
                          setTimeout(function () {
                            e.touch || (e.hover = !1);
                          }, this.hoverStayTime));
                    },
                  },
                  {
                    key: "componentDidRender",
                    value: function componentDidRender() {
                      handleStencilNodes(this.el);
                    },
                  },
                  {
                    key: "render",
                    value: function render() {
                      var e = (0, u.c)(
                          (0, a.A)({}, "".concat(this.hoverClass), this.hover)
                        ),
                        t = {};
                      return (
                        this.animation &&
                          ((t.animation = this.animation),
                          (t["data-animation"] = this.animation)),
                        (0, d.h)(
                          d.xr,
                          Object.assign(
                            {
                              class: e,
                            },
                            t
                          ),
                          (0, d.h)("slot", null)
                        )
                      );
                    },
                  },
                  {
                    key: "el",
                    get: function get() {
                      return this;
                    },
                  },
                ],
                [
                  {
                    key: "style",
                    get: function get() {
                      return "body,html{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;-webkit-tap-highlight-color:rgba(0, 0, 0, 0)}taro-view-core{display:block}";
                    },
                  },
                ]
              )
            );
          })(d.wt),
          [
            4,
            "taro-view-core",
            {
              animation: [1],
              hoverClass: [1, "hover-class"],
              hoverStartTime: [2, "hover-start-time"],
              hoverStayTime: [2, "hover-stay-time"],
              hover: [32],
              touch: [32],
            },
            [
              [1, "touchstart", "onTouchStart"],
              [1, "touchmove", "onTouchMove"],
              [1, "touchend", "onTouchEnd"],
            ],
          ]
        );
      var ee = function taro_view_core_defineCustomElement$1() {
        if ("undefined" != typeof customElements) {
          ["taro-view-core"].forEach(function (e) {
            if ("taro-view-core" === e)
              customElements.get(e) || customElements.define(e, K);
          });
        }
      };
      Object.create;
      Object.create;
      var te = function mergeRefs() {
          for (var e = arguments.length, t = new Array(e), n = 0; n < e; n++)
            t[n] = arguments[n];
          return function (e) {
            t.forEach(function (t) {
              !(function setRef(e, t) {
                "function" == typeof e ? e(t) : null != e && (e.current = t);
              })(t, e);
            });
          };
        },
        ne = function camelToDashCase(e) {
          return e.replace(/([A-Z])/g, function (e) {
            return "-".concat(e[0].toLowerCase());
          });
        },
        ie = function arrayToMap(e) {
          var t = new Map();
          return (
            e.forEach(function (e) {
              return t.set(e, e);
            }),
            t
          );
        },
        re = function syncEvent(e, t, n) {
          var i = e.__events || (e.__events = {}),
            r = i[t];
          r && e.removeEventListener(t, r),
            e.addEventListener(
              t,
              (i[t] = function handler(e) {
                n && n.call(this, e);
              })
            );
        },
        ae = function attachProps(e, t) {
          var n =
            arguments.length > 2 && void 0 !== arguments[2] ? arguments[2] : {};
          e instanceof Element &&
            (Object.keys(n).forEach(function (n) {
              if (
                ![
                  "style",
                  "children",
                  "ref",
                  "class",
                  "className",
                  "forwardedRef",
                ].includes(n) &&
                !t.hasOwnProperty(n)
              )
                if (/^on([A-Z].*)/.test(n)) {
                  var i = n.substring(2).toLowerCase();
                  re(e, i, void 0);
                } else (e[n] = null), e.removeAttribute(ne(n));
            }),
            (e.className = (function getClassName(e, t, n) {
              var i = t.className || t.class,
                r = n.className || n.class,
                a = ie(e),
                o = ie(i ? i.split(" ") : []),
                s = ie(r ? r.split(" ") : []),
                l = [];
              return (
                a.forEach(function (e) {
                  o.has(e) ? (l.push(e), o.delete(e)) : s.has(e) || l.push(e);
                }),
                o.forEach(function (e) {
                  return l.push(e);
                }),
                l.join(" ")
              );
            })(e.classList, t, n)),
            Object.keys(t).forEach(function (n) {
              if (
                !(
                  ("style" === n && "string" != typeof t[n]) ||
                  [
                    "children",
                    "ref",
                    "class",
                    "className",
                    "forwardedRef",
                  ].includes(n)
                )
              )
                if (/^on([A-Z].*)/.test(n)) {
                  var i = n.substring(2).toLowerCase();
                  re(e, i, t[n]);
                } else {
                  (e[n] = t[n]),
                    "string" === (0, N.A)(t[n]) && e.setAttribute(ne(n), t[n]);
                }
            }));
        },
        oe = function createReactComponent(e, t, n, r) {
          void 0 !== r && r();
          var a = (function dashToPascalCase(e) {
              return e
                .toLowerCase()
                .split("-")
                .map(function (e) {
                  return e.charAt(0).toUpperCase() + e.slice(1);
                })
                .join("");
            })(e),
            d = (function (t) {
              function ReactComponent(e) {
                var t;
                return (
                  (0, o.A)(this, ReactComponent),
                  ((t = (0, l.A)(this, ReactComponent, [e])).setComponentElRef =
                    function (e) {
                      t.componentEl = e;
                    }),
                  t
                );
              }
              return (
                (0, c.A)(ReactComponent, t),
                (0, s.A)(
                  ReactComponent,
                  [
                    {
                      key: "componentDidMount",
                      value: function componentDidMount() {
                        this.componentDidUpdate(this.props);
                      },
                    },
                    {
                      key: "componentDidUpdate",
                      value: function componentDidUpdate(e) {
                        ae(this.componentEl, this.props, e);
                      },
                    },
                    {
                      key: "render",
                      value: function render() {
                        var t = this.props,
                          r = t.children,
                          a = t.forwardedRef,
                          o =
                            (t.className,
                            t.ref,
                            t.style,
                            (function __rest(e, t) {
                              var n = {};
                              for (var i in e)
                                Object.prototype.hasOwnProperty.call(e, i) &&
                                  t.indexOf(i) < 0 &&
                                  (n[i] = e[i]);
                              if (
                                null != e &&
                                "function" ==
                                  typeof Object.getOwnPropertySymbols
                              ) {
                                var r = 0;
                                for (
                                  i = Object.getOwnPropertySymbols(e);
                                  r < i.length;
                                  r++
                                )
                                  t.indexOf(i[r]) < 0 &&
                                    Object.prototype.propertyIsEnumerable.call(
                                      e,
                                      i[r]
                                    ) &&
                                    (n[i[r]] = e[i[r]]);
                              }
                              return n;
                            })(t, [
                              "children",
                              "forwardedRef",
                              "className",
                              "ref",
                              "style",
                            ])),
                          s = Object.keys(o).reduce(function (e, t) {
                            var n = o[t];
                            if (
                              0 === t.indexOf("on") &&
                              t[2] === t[2].toUpperCase()
                            ) {
                              t.substring(2).toLowerCase();
                              0;
                            } else {
                              var i = (0, N.A)(n);
                              ["string", "boolean", "number"].includes(i) &&
                                (e[ne(t)] = n);
                            }
                            return e;
                          }, {});
                        n && (s = n(this.props, s));
                        var l = Object.assign(Object.assign({}, s), {
                          ref: te(a, this.setComponentElRef),
                        });
                        return (0, i.createElement)(e, l, r);
                      },
                    },
                  ],
                  [
                    {
                      key: "displayName",
                      get: function get() {
                        return a;
                      },
                    },
                  ]
                )
              );
            })(i.Component);
          return (
            t && (d.contextType = t),
            (function createForwardRef(e, t) {
              var n = function forwardRef(t, n) {
                return i.createElement(
                  e,
                  Object.assign({}, t, {
                    forwardedRef: n,
                  })
                );
              };
              return (n.displayName = t), i.forwardRef(n);
            })(d, a)
          );
        },
        se = i.Fragment,
        le = oe("taro-button-core", void 0, r, h),
        ce = oe("taro-canvas-core", void 0, r, v),
        de = oe("taro-custom-wrapper-core", void 0, r, b),
        ue = oe("taro-form-core", void 0, r, A),
        pe = oe("taro-icon-core", void 0, r, _),
        he = oe("taro-image-core", void 0, r, S),
        me = oe("taro-input-core", void 0, r, M),
        fe = oe("taro-navigator-core", void 0, r, P),
        ve = oe("taro-rich-text-core", void 0, r, D),
        ge = oe("taro-scroll-view-core", void 0, r, B),
        we = oe("taro-swiper-core", void 0, r, R.X),
        be = oe("taro-swiper-item-core", void 0, r, F.X),
        ye = oe("taro-text-core", void 0, r, G),
        Ae = oe("taro-video-core", void 0, r, J),
        xe = oe("taro-view-core", void 0, r, ee);
    },
    1472: function (e, t, n) {
      "use strict";
      n.d(t, {
        j5: function () {
          return r;
        },
      });
      var i = n(7092),
        r = function getSystemInfoSync() {
          var e = (function getWindowInfo() {
              return {
                pixelRatio: window.devicePixelRatio,
                screenWidth: window.screen.width,
                screenHeight: window.screen.height,
                windowWidth: document.documentElement.clientWidth,
                windowHeight: document.documentElement.clientHeight,
                statusBarHeight: NaN,
                safeArea: {
                  bottom: 0,
                  height: 0,
                  left: 0,
                  right: 0,
                  top: 0,
                  width: 0,
                },
              };
            })(),
            t = (function getSystemSetting() {
              return {
                bluetoothEnabled: !1,
                locationEnabled: !1,
                wifiEnabled: !1,
                deviceOrientation:
                  window.screen.width >= window.screen.height
                    ? "landscape"
                    : "portrait",
              };
            })(),
            n = (function getDeviceInfo() {
              var e = (0, i.a3)();
              return {
                abi: "",
                deviceAbi: "",
                benchmarkLevel: -1,
                brand: e.mobile() || "",
                model: e.mobile() || "",
                system: e.os(),
                platform: navigator.platform,
                CPUType: "",
              };
            })(),
            r = (function getAppBaseInfo() {
              var e,
                t = !1;
              return (
                (null === (e = window.matchMedia) || void 0 === e
                  ? void 0
                  : e.call(window, "(prefers-color-scheme: dark)").matches) &&
                  (t = !0),
                {
                  SDKVersion: "",
                  enableDebug: !1,
                  language: navigator.language,
                  version: "",
                  theme: t ? "dark" : "light",
                }
              );
            })(),
            a = {
              albumAuthorized: "not determined",
              bluetoothAuthorized: "not determined",
              cameraAuthorized: "not determined",
              locationAuthorized: "not determined",
              locationReducedAccuracy: !1,
              microphoneAuthorized: "not determined",
              notificationAuthorized: "not determined",
              notificationAlertAuthorized: "not determined",
              notificationBadgeAuthorized: "not determined",
              notificationSoundAuthorized: "not determined",
              phoneCalendarAuthorized: "not determined",
            };
          return (
            delete n.abi,
            Object.assign(
              Object.assign(
                Object.assign(Object.assign(Object.assign({}, e), t), n),
                r
              ),
              {
                fontSizeSetting: NaN,
                albumAuthorized: "authorized" === a.albumAuthorized,
                cameraAuthorized: "authorized" === a.cameraAuthorized,
                locationAuthorized: "authorized" === a.locationAuthorized,
                microphoneAuthorized: "authorized" === a.microphoneAuthorized,
                notificationAuthorized:
                  "authorized" === a.notificationAuthorized,
                notificationAlertAuthorized:
                  "authorized" === a.notificationAlertAuthorized,
                notificationBadgeAuthorized:
                  "authorized" === a.notificationBadgeAuthorized,
                notificationSoundAuthorized:
                  "authorized" === a.notificationSoundAuthorized,
                phoneCalendarAuthorized:
                  "authorized" === a.phoneCalendarAuthorized,
                locationReducedAccuracy: a.locationReducedAccuracy,
                environment: "",
              }
            )
          );
        };
    },
    4263: function (e, t, n) {
      "use strict";
      n.d(t, {
        J: function () {
          return h;
        },
      });
      var i = n(5544),
        r = n(2901),
        a = n(3029),
        o = (0, r.A)(function StyleSheet() {
          var e = this;
          (0, a.A)(this, StyleSheet),
            (this.$style = null),
            (this.sheet = null),
            (this.appendStyleSheet = function () {
              if (e.$style) {
                var t = document.getElementsByTagName("head")[0];
                e.$style.setAttribute("type", "text/css"),
                  e.$style.setAttribute("data-type", "Taro"),
                  t.appendChild(e.$style),
                  (e.sheet = e.$style.sheet);
              }
              e.sheet &&
                !("insertRule" in e.sheet) &&
                console.warn("当前浏览器不支持 stylesheet.insertRule 接口");
            }),
            (this.add = function (t) {
              var n,
                i =
                  arguments.length > 1 && void 0 !== arguments[1]
                    ? arguments[1]
                    : 0;
              null === e.sheet && e.appendStyleSheet(),
                null === (n = e.sheet) || void 0 === n || n.insertRule(t, i);
            }),
            (this.$style = document.createElement("style"));
        }),
        s = new o(),
        l = "transitionend",
        c = "transform",
        d = document.createElement("div");
      (d.style.cssText =
        "-webkit-animation-name:webkit;-moz-animation-name:moz;-ms-animation-name:ms;animation-name:standard;"),
        "standard" === d.style["animation-name"]
          ? ((l = "transitionend"), (c = "transform"))
          : "webkit" === d.style["-webkit-animation-name"]
          ? ((l = "webkitTransitionEnd"), (c = "-webkit-transform"))
          : "moz" === d.style["-moz-animation-name"]
          ? ((l = "mozTransitionEnd"), (c = "-moz-transform"))
          : "ms" === d.style["-ms-animation-name"] &&
            ((l = "msTransitionEnd"), (c = "-ms-transform"));
      var u = 0,
        p = (function () {
          return (0, r.A)(
            function Animation() {
              var e = this,
                t =
                  arguments.length > 0 && void 0 !== arguments[0]
                    ? arguments[0]
                    : {},
                n = t.duration,
                r = void 0 === n ? 400 : n,
                o = t.delay,
                s = void 0 === o ? 0 : o,
                d = t.timingFunction,
                p = void 0 === d ? "linear" : d,
                h = t.transformOrigin,
                m = void 0 === h ? "50% 50% 0" : h,
                f = t.unit,
                v = void 0 === f ? "px" : f;
              (0, a.A)(this, Animation),
                (this.rules = []),
                (this.transform = ["".concat(c, ":")]),
                (this.steps = []),
                (this.animationMap = {}),
                (this.animationMapCount = 0),
                this.setDefault(r, s, p, m),
                (this.unit = v);
              var g = "animation";
              (this.id = ++u),
                document.body.addEventListener(l, function (t) {
                  var n = t.target;
                  null === n.getAttribute(g) && (g = "data-animation");
                  var r = n.getAttribute(g);
                  if (null !== r) {
                    var a = r.split("__"),
                      o = (0, i.A)(a, 2),
                      s = o[0],
                      l = o[1];
                    if (
                      s ===
                      "taro-h5-poly-fill/".concat(e.id, "/create-animation")
                    ) {
                      var c = l.split("--"),
                        d = (0, i.A)(c, 2),
                        u = d[0],
                        p = d[1],
                        h = Number(void 0 === p ? 0 : p);
                      h < e.animationMap["".concat(s, "__").concat(u)] - 1 &&
                        (n.setAttribute(
                          g,
                          ""
                            .concat(s, "__")
                            .concat(u, "--")
                            .concat(h + 1)
                        ),
                        "animation" === g &&
                          n.setAttribute(
                            "data-animation",
                            ""
                              .concat(s, "__")
                              .concat(u, "--")
                              .concat(h + 1)
                          ));
                    }
                  }
                });
            },
            [
              {
                key: "transformUnit",
                value: function transformUnit() {
                  for (
                    var e = this,
                      t = [],
                      n = arguments.length,
                      i = new Array(n),
                      r = 0;
                    r < n;
                    r++
                  )
                    i[r] = arguments[r];
                  return (
                    i.forEach(function (n) {
                      t.push(isNaN(n) ? n : "".concat(n).concat(e.unit));
                    }),
                    t
                  );
                },
              },
              {
                key: "setDefault",
                value: function setDefault(e, t, n, i) {
                  this.DEFAULT = {
                    duration: e,
                    delay: t,
                    timingFunction: n,
                    transformOrigin: i,
                  };
                },
              },
              {
                key: "matrix",
                value: function matrix(e, t, n, i, r, a) {
                  return (
                    this.transform.push(
                      "matrix("
                        .concat(e, ", ")
                        .concat(t, ", ")
                        .concat(n, ", ")
                        .concat(i, ", ")
                        .concat(r, ", ")
                        .concat(a, ")")
                    ),
                    this
                  );
                },
              },
              {
                key: "matrix3d",
                value: function matrix3d(
                  e,
                  t,
                  n,
                  i,
                  r,
                  a,
                  o,
                  s,
                  l,
                  c,
                  d,
                  u,
                  p,
                  h,
                  m,
                  f
                ) {
                  return (
                    this.transform.push(
                      "matrix3d("
                        .concat(e, ", ")
                        .concat(t, ", ")
                        .concat(n, ", ")
                        .concat(i, ", ")
                        .concat(r, ", ")
                        .concat(a, ", ")
                        .concat(o, ", ")
                        .concat(s, ", ")
                        .concat(l, ", ")
                        .concat(c, ", ")
                        .concat(d, ", ")
                        .concat(u, ", ")
                        .concat(p, ", ")
                        .concat(h, ", ")
                        .concat(m, ", ")
                        .concat(f, ")")
                    ),
                    this
                  );
                },
              },
              {
                key: "rotate",
                value: function rotate(e) {
                  return this.transform.push("rotate(".concat(e, "deg)")), this;
                },
              },
              {
                key: "rotate3d",
                value: function rotate3d(e, t, n, i) {
                  return (
                    "number" != typeof t
                      ? this.transform.push("rotate3d(".concat(e, ")"))
                      : this.transform.push(
                          "rotate3d("
                            .concat(e, ", ")
                            .concat(t || 0, ", ")
                            .concat(n || 0, ", ")
                            .concat(i || 0, "deg)")
                        ),
                    this
                  );
                },
              },
              {
                key: "rotateX",
                value: function rotateX(e) {
                  return (
                    this.transform.push("rotateX(".concat(e, "deg)")), this
                  );
                },
              },
              {
                key: "rotateY",
                value: function rotateY(e) {
                  return (
                    this.transform.push("rotateY(".concat(e, "deg)")), this
                  );
                },
              },
              {
                key: "rotateZ",
                value: function rotateZ(e) {
                  return (
                    this.transform.push("rotateZ(".concat(e, "deg)")), this
                  );
                },
              },
              {
                key: "scale",
                value: function scale(e, t) {
                  return (
                    this.transform.push(
                      "scale(".concat(e, ", ").concat(t, ")")
                    ),
                    this
                  );
                },
              },
              {
                key: "scale3d",
                value: function scale3d(e, t, n) {
                  return (
                    this.transform.push(
                      "scale3d(".concat(e, ", ").concat(t, ", ").concat(n, ")")
                    ),
                    this
                  );
                },
              },
              {
                key: "scaleX",
                value: function scaleX(e) {
                  return this.transform.push("scaleX(".concat(e, ")")), this;
                },
              },
              {
                key: "scaleY",
                value: function scaleY(e) {
                  return this.transform.push("scaleY(".concat(e, ")")), this;
                },
              },
              {
                key: "scaleZ",
                value: function scaleZ(e) {
                  return this.transform.push("scaleZ(".concat(e, ")")), this;
                },
              },
              {
                key: "skew",
                value: function skew(e, t) {
                  return (
                    this.transform.push("skew(".concat(e, ", ").concat(t, ")")),
                    this
                  );
                },
              },
              {
                key: "skewX",
                value: function skewX(e) {
                  return this.transform.push("skewX(".concat(e, ")")), this;
                },
              },
              {
                key: "skewY",
                value: function skewY(e) {
                  return this.transform.push("skewY(".concat(e, ")")), this;
                },
              },
              {
                key: "translate",
                value: function translate(e, t) {
                  var n = this.transformUnit(e, t),
                    r = (0, i.A)(n, 2);
                  return (
                    (e = r[0]),
                    (t = r[1]),
                    this.transform.push(
                      "translate(".concat(e, ", ").concat(t, ")")
                    ),
                    this
                  );
                },
              },
              {
                key: "translate3d",
                value: function translate3d(e, t, n) {
                  var r = this.transformUnit(e, t, n),
                    a = (0, i.A)(r, 3);
                  return (
                    (e = a[0]),
                    (t = a[1]),
                    (n = a[2]),
                    this.transform.push(
                      "translate3d("
                        .concat(e, ", ")
                        .concat(t, ", ")
                        .concat(n, ")")
                    ),
                    this
                  );
                },
              },
              {
                key: "translateX",
                value: function translateX(e) {
                  var t = this.transformUnit(e);
                  return (
                    (e = (0, i.A)(t, 1)[0]),
                    this.transform.push("translateX(".concat(e, ")")),
                    this
                  );
                },
              },
              {
                key: "translateY",
                value: function translateY(e) {
                  var t = this.transformUnit(e);
                  return (
                    (e = (0, i.A)(t, 1)[0]),
                    this.transform.push("translateY(".concat(e, ")")),
                    this
                  );
                },
              },
              {
                key: "translateZ",
                value: function translateZ(e) {
                  var t = this.transformUnit(e);
                  return (
                    (e = (0, i.A)(t, 1)[0]),
                    this.transform.push("translateZ(".concat(e, ")")),
                    this
                  );
                },
              },
              {
                key: "opacity",
                value: function opacity(e) {
                  return this.rules.push("opacity: ".concat(e)), this;
                },
              },
              {
                key: "backgroundColor",
                value: function backgroundColor(e) {
                  return this.rules.push("background-color: ".concat(e)), this;
                },
              },
              {
                key: "width",
                value: function width(e) {
                  var t = this.transformUnit(e);
                  return (
                    (e = (0, i.A)(t, 1)[0]),
                    this.rules.push("width: ".concat(e)),
                    this
                  );
                },
              },
              {
                key: "height",
                value: function height(e) {
                  var t = this.transformUnit(e);
                  return (
                    (e = (0, i.A)(t, 1)[0]),
                    this.rules.push("height: ".concat(e)),
                    this
                  );
                },
              },
              {
                key: "top",
                value: function top(e) {
                  var t = this.transformUnit(e);
                  return (
                    (e = (0, i.A)(t, 1)[0]),
                    this.rules.push("top: ".concat(e)),
                    this
                  );
                },
              },
              {
                key: "right",
                value: function right(e) {
                  var t = this.transformUnit(e);
                  return (
                    (e = (0, i.A)(t, 1)[0]),
                    this.rules.push("right: ".concat(e)),
                    this
                  );
                },
              },
              {
                key: "bottom",
                value: function bottom(e) {
                  var t = this.transformUnit(e);
                  return (
                    (e = (0, i.A)(t, 1)[0]),
                    this.rules.push("bottom: ".concat(e)),
                    this
                  );
                },
              },
              {
                key: "left",
                value: function left(e) {
                  var t = this.transformUnit(e);
                  return (
                    (e = (0, i.A)(t, 1)[0]),
                    this.rules.push("left: ".concat(e)),
                    this
                  );
                },
              },
              {
                key: "step",
                value: function step() {
                  var e =
                      arguments.length > 0 && void 0 !== arguments[0]
                        ? arguments[0]
                        : {},
                    t = this.DEFAULT,
                    n = e.duration,
                    i = void 0 === n ? t.duration : n,
                    r = e.delay,
                    a = void 0 === r ? t.delay : r,
                    o = e.timingFunction,
                    s = void 0 === o ? t.timingFunction : o,
                    l = e.transformOrigin,
                    d = void 0 === l ? t.transformOrigin : l;
                  return (
                    this.steps.push(
                      [
                        this.rules
                          .map(function (e) {
                            return "".concat(e, "!important");
                          })
                          .join(";"),
                        "".concat(this.transform.join(" "), "!important"),
                        "".concat(c, "-origin: ").concat(d),
                        "transition: all "
                          .concat(i, "ms ")
                          .concat(s, " ")
                          .concat(a, "ms"),
                      ]
                        .filter(function (e) {
                          return "" !== e && e !== "".concat(c, ":");
                        })
                        .join(";")
                    ),
                    (this.rules = []),
                    (this.transform = ["".concat(c, ":")]),
                    this
                  );
                },
              },
              {
                key: "createAnimationData",
                value: function createAnimationData() {
                  var e = "taro-h5-poly-fill/"
                    .concat(this.id, "/create-animation__")
                    .concat(this.animationMapCount++);
                  return (
                    (this.animationMap[e] = this.steps.length),
                    this.steps.forEach(function (t, n) {
                      var i =
                        0 === n
                          ? '[animation="'
                              .concat(e, '"], [data-animation="')
                              .concat(e, '"]')
                          : '[animation="'
                              .concat(e, "--")
                              .concat(n, '"], [data-animation="')
                              .concat(e, "--")
                              .concat(n, '"]');
                      s.add("".concat(i, " { ").concat(t, " }"));
                    }),
                    (this.steps = []),
                    e
                  );
                },
              },
              {
                key: "export",
                value: function _export() {
                  return this.createAnimationData();
                },
              },
            ]
          );
        })(),
        h = function createAnimation(e) {
          return new p(e);
        };
    },
    4462: function (e, t, n) {
      "use strict";
      var i = n(9379),
        r = n(3986),
        a = n(7289),
        o = n(4353),
        s = n.n(o),
        l = n(4848),
        c = ["throttleTime"];
      t.A = function View(e) {
        var t = e.throttleTime,
          n = void 0 === t ? 1e3 : t,
          o = (0, r.A)(e, c),
          d = (function throttle(e) {
            var t =
              arguments.length > 1 && void 0 !== arguments[1]
                ? arguments[1]
                : 1e3;
            if (e) {
              var n = s()().valueOf(),
                i = !0;
              return function () {
                var r = s()().valueOf();
                console.error("now - previous: 点击价格", r - n, i);
                for (
                  var a = arguments.length, o = new Array(a), l = 0;
                  l < a;
                  l++
                )
                  o[l] = arguments[l];
                i
                  ? (e && e.apply(this, o), (i = !1), (n = s()().valueOf()))
                  : r - n > t && ((n = r), (i = !1), e && e.apply(this, o));
              };
            }
          })(null == e ? void 0 : e.onClick, n);
        return (0, l.jsx)(
          a.Ss,
          (0, i.A)(
            (0, i.A)({}, o),
            {},
            {
              onClick: d,
            }
          )
        );
      };
    },
    3940: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return E;
        },
      });
      var i = n(9379),
        r = n(6540),
        a = n(7289),
        o = n(4462),
        s = n(4025),
        l = n(1850),
        c = n(488),
        d = n(4558),
        u = n(6618),
        p = n(9272),
        h = n(7908),
        m = n(3272),
        f = "index-modules__globalHeader___fSJPU",
        v = "index-modules__headerInfo___kjaEl",
        g = "index-modules__imageContent___fhKT5",
        w = "index-modules__image___L6VUi",
        b = "index-modules__headerLeft___qrzZd",
        y = "index-modules__img___zmCbS",
        A = "index-modules__headerRight___ZKZuV",
        x = "index-modules__headerCenter___IrrXn",
        _ = n(4848);
      var E = function Header(e) {
        var t = e.backgroundColor,
          n = void 0 === t ? "#fff" : t,
          E = e.titleColor,
          S = void 0 === E ? "#000" : E,
          C = e.outStyle,
          T = void 0 === C ? {} : C,
          k = e.titleStyle,
          M = void 0 === k ? {} : k,
          I = e.imageStyle,
          L = void 0 === I ? {} : I,
          P = e.title,
          N = void 0 === P ? "首页" : P,
          O = e.hasLeft,
          D = void 0 === O || O,
          z = e.arrowLeft,
          j =
            void 0 === z
              ? "https://gw.alicdn.com/imgextra/i1/O1CN01rJPmxF1O8mjvn7dwd_!!6000000001661-2-tps-45-76.png"
              : z,
          B = e.isCustomLeftClick,
          R = void 0 !== B && B,
          F = e.image,
          Y = e.onCustomLeftClick,
          G = e.dingPageShareinfo,
          V = (0, d.A)(),
          W = s.Ay.getCurrentPages().length,
          H = (0, r.useMemo)(
            function () {
              var e = {
                  background: n,
                },
                t = {
                  height: "".concat(
                    null == V ? void 0 : V.statusBarHeight,
                    "px"
                  ),
                },
                i = {
                  height: null == V ? void 0 : V.getHeight,
                };
              return {
                backHomeTrue: W <= 1,
                headerBoxStyle: e,
                statusBoxStyle: t,
                headerInfoStyle: i,
                ledongliStyle: {
                  paddingTop:
                    u.default.isLedongli && u.default.isAndroid
                      ? (0, s.n_)(null == V ? void 0 : V.top)
                      : void 0,
                },
              };
            },
            [n, V, W]
          ),
          X = (0, r.useMemo)(
            function () {
              var e = V;
              return (
                u.default.isAlipay || (u.default.isDingTalk && u.default.isIOS)
                  ? (e.top = (null == e ? void 0 : e.statusBarHeight) || 47)
                  : u.default.isLedongli
                  ? (e.top =
                      null !== u.default &&
                      void 0 !== u.default &&
                      u.default.isIOS
                        ? (null == e ? void 0 : e.top) || 44
                        : (0, s.n_)((null == V ? void 0 : V.top) || 48))
                  : (e.top = 0),
                {
                  top: "".concat(null == e ? void 0 : e.top, "px"),
                  height:
                    null != e && e.height
                      ? "".concat((null == e ? void 0 : e.height) || 0, "px ")
                      : null == e
                      ? void 0
                      : e.getHeight,
                  paddingLeft:
                    u.default.isAlipay || u.default.isDingTalk
                      ? "".concat((0, s.n_)(44))
                      : "".concat((0, s.n_)(0)),
                }
              );
            },
            [V]
          ),
          U = function dingPageShare() {
            var e = G || {},
              t = e.title,
              n = void 0 === t ? "乐动力体育中心官方预订平台" : t,
              i = e.descContent,
              r = void 0 === i ? "在线便捷预订&购买 享受更多福利~" : i,
              a = e.iconImage,
              o =
                void 0 === a
                  ? "https://gw.alicdn.com/imgextra/i2/O1CN01P0WNY31e3Jqxh8tSL_!!6000000003815-0-tps-850-850.jpg"
                  : a,
              s = e.path,
              l = "https://market."
                .concat(
                  u.default.isPre ? "wapa" : "m",
                  ".taobao.com/app/alisports-fe/sports-gym-client/h5/index.html#"
                )
                .concat(s);
            (0, p.uM)({
              title: n,
              descContent: r,
              iconImage: o,
              url: l,
            });
          },
          Z = (0, r.useCallback)(
            function () {
              R
                ? null == Y || Y()
                : u.default.isDingTalk
                ? 1 === W
                  ? c.A.popWindow()
                  : c.A.back()
                : 1 !== W
                ? (0, l.Tl)()
                : (0, l.OG)({
                    url: "/pages/Index/Index/index",
                  });
            },
            [W]
          );
        return (
          (0, h.A)(function (e) {
            !e && u.default.isAlipay && (0, m.BM)(S);
          }),
          (0, _.jsxs)(o.A, {
            className: f,
            style: (0, i.A)(
              (0, i.A)(
                (0, i.A)({}, H.headerBoxStyle),
                {},
                {
                  color: S,
                },
                T
              ),
              null == H ? void 0 : H.ledongliStyle
            ),
            children: [
              (0, _.jsx)(o.A, {
                style: H.statusBoxStyle,
              }),
              (0, _.jsx)(o.A, {
                className: v,
                style: H.headerInfoStyle,
                children: F
                  ? (0, _.jsxs)(o.A, {
                      className: g,
                      style: X,
                      children: [
                        u.default.isDingTalk &&
                          (0, _.jsx)(o.A, {
                            className: b,
                            onClick: Z,
                            children: (0, _.jsx)(a._V, {
                              className: y,
                              webp: !0,
                              mode: "aspectFill",
                              src: j,
                            }),
                          }),
                        (0, _.jsx)(a._V, {
                          className: w,
                          src: F,
                          style: L,
                          onClick: Z,
                        }),
                        u.default.isDingTalk &&
                          G &&
                          (0, _.jsx)(o.A, {
                            className: A,
                            onClick: U,
                            children: (0, _.jsx)(a._V, {
                              className: y,
                              webp: !0,
                              mode: "aspectFill",
                              src:
                                "#fff" === S
                                  ? "https://gw.alicdn.com/imgextra/i4/O1CN01BwEyXX1TKIYHT3O8y_!!6000000002363-2-tps-64-64.png"
                                  : "https://gw.alicdn.com/imgextra/i1/O1CN01sCYfTu28fGw2eMonD_!!6000000007959-2-tps-64-64.png",
                            }),
                          }),
                      ],
                    })
                  : (0, _.jsxs)(_.Fragment, {
                      children: [
                        D
                          ? (0, _.jsx)(o.A, {
                              className: b,
                              onClick: Z,
                              style: {
                                opacity:
                                  u.default.isAlipay &&
                                  s.Ay.getCurrentPages().length >= 1
                                    ? 0
                                    : 1,
                              },
                              children: (0, _.jsx)(a._V, {
                                className: y,
                                webp: !0,
                                mode: "aspectFill",
                                src:
                                  "#fff" === S
                                    ? "https://gw.alicdn.com/imgextra/i4/O1CN01VM2URk1cVb5TlaBqR_!!6000000003606-2-tps-45-76.png"
                                    : j,
                              }),
                            })
                          : null,
                        (0, _.jsx)(o.A, {
                          className: x,
                          style: (0, i.A)(
                            {
                              color: S || "#000",
                            },
                            M
                          ),
                          children: N,
                        }),

                        G &&
                          (0, _.jsx)(o.A, {
                            className: A,
                            onClick: U,
                            children: (0, _.jsx)(a._V, {
                              className: y,
                              webp: !0,
                              mode: "aspectFill",
                              src:
                                "#fff" === S
                                  ? "https://gw.alicdn.com/imgextra/i4/O1CN01BwEyXX1TKIYHT3O8y_!!6000000002363-2-tps-64-64.png"
                                  : "https://gw.alicdn.com/imgextra/i1/O1CN01sCYfTu28fGw2eMonD_!!6000000007959-2-tps-64-64.png",
                            }),
                          }),
                      ],
                    }),
              }),
            ],
          })
        );
      };
    },
    5428: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return b;
        },
      });
      var i = n(6738),
        r = n(4566),
        a = n(7289),
        o = n(4462),
        s = n(6942),
        l = n.n(s),
        c = "index-module__gymActionSheetTitle___XqbyC",
        d = "index-module__sheetTitle___lDkBY",
        u = "index-module__T___zz2LZ",
        p = "index-module__titleDesc___cyt56",
        h = "index-module__sheetInfo___xvybJ",
        m = "index-module__sheetInfoDisabled___BF7Cu",
        f = "index-module__sheetInfoHighlight___t1R1n",
        v = "index-module__img___okcEn",
        g = "index-module__gymActionSheetWrap___tscFs",
        w = n(4848);
      var b = function LeftACtionSheet(e) {
        var t = e.show,
          n = e.onClose,
          s = e.onCancel,
          b = e.title,
          y = e.children,
          A = e.cancelText,
          x = void 0 === A ? "" : A,
          _ = e.themeVars,
          E = void 0 === _ ? {} : _,
          S = e.labelName,
          C = void 0 === S ? "" : S,
          T = e.isHighlightLabel,
          k = void 0 !== T && T,
          M = e.closeIcon,
          I = void 0 === M || M,
          L = e.safeAreaInsetBottom,
          P = void 0 === L || L,
          N = e.onCloseOverlay,
          O = e.titleDesc;
        return (0, w.jsx)(i.A, {
          themeVars: E,
          children: (0, w.jsx)(r.A, {
            show: t,
            cancelText: x,
            onClose: function onClose() {
              N ? N() : n();
            },
            onCancel: s,
            safeAreaInsetBottom: P,
            children: (0, w.jsxs)(w.Fragment, {
              children: [
                (0, w.jsxs)(o.A, {
                  className: c,
                  children: [
                    (0, w.jsxs)(o.A, {
                      className: d,
                      children: [
                        (0, w.jsx)(o.A, {
                          className: u,
                          children: b,
                        }),
                        C &&
                          (0, w.jsx)(o.A, {
                            className: l()(h, k ? f : m),
                            children: C,
                          }),
                        O &&
                          (0, w.jsx)(o.A, {
                            className: p,
                            children: O,
                          }),
                      ],
                    }),
                    I
                      ? (0, w.jsx)(a._V, {
                          onClick: function onClick() {
                            n && n();
                          },
                          className: v,
                          webp: !0,
                          mode: "aspectFill",
                          src: "https://gw.alicdn.com/imgextra/i4/O1CN01Pofsjb1ikbj1XEPxR_!!6000000004451-49-tps-96-96.webp",
                        })
                      : null,
                  ],
                }),
                (0, w.jsx)(o.A, {
                  className: g,
                  children: y,
                }),
              ],
            }),
          }),
        });
      };
    },
    9839: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return S;
        },
      });
      var i = n(5460),
        r = n(9379),
        a = n(4467),
        o = n(6185),
        s = n(4025),
        l = n(7289),
        c = n(6942),
        d = n.n(c),
        u = "index-module__dialog___fJsA_",
        p = "index-module__popupStyle___wevLk",
        h = "index-module__title___TP6O2",
        m = "index-module__titleDesc___bvQ2M",
        f = "index-module__icon___lenTy",
        v = "index-module__content___YKKFd",
        g = "index-module__footer___tjXC_",
        w = "index-module__btnGroup___ounQF",
        b = "index-module__cancel___TnlgD",
        y = "index-module__confirm___irTG0",
        A = "index-module__disabled___EYjvg",
        x = "index-module__btn___OtnVA",
        _ = "index-module__desc___uudOT",
        E = n(4848);
      var S = function Modal(e) {
        var t = e.show,
          n = e.title,
          c = e.closeable,
          S = void 0 === c || c,
          C = e.onClose,
          T = e.children,
          k = e.cancelText,
          M = e.confirmText,
          I = e.descText,
          L = e.onConfirm,
          P = e.onDescClick,
          N = e.onCancel,
          O = e.confirmBtnConfig,
          D = (void 0 === O ? {} : O).disabled,
          z = e.contentStyle,
          j = e.titleDesc,
          B = e.titleStyle,
          R = e.titleDescStyle,
          F = e.confirmTextStyle,
          Y = e.closeOnClickOverlay,
          G = void 0 === Y || Y,
          V = e.descStyle,
          W = e.confirmProps,
          H = e.popupStyle;
        return (0, E.jsx)(l.Ss, {
          className: u,
          onTouchMove: function onTouchMove(e) {
            e.stopPropagation(), e.preventDefault();
          },
          onClick: function onClick(e) {
            e.stopPropagation(), e.preventDefault();
          },
          children: (0, E.jsxs)(i.A, {
            closeOnClickOverlay: G,
            overlay: !0,
            className: p,
            show: t,
            onClose: C,
            style: H,
            children: [
              n &&
                (0, E.jsx)(l.Ss, {
                  className: h,
                  style: B,
                  children: n,
                }),
              j &&
                (0, E.jsx)(l.Ss, {
                  className: m,
                  style: R,
                  children: j,
                }),
              S &&
                (0, E.jsx)(o.A, {
                  name: "cross",
                  size: (0, s.n_)(40),
                  className: f,
                  onClick: C,
                }),
              (0, E.jsx)(l.BM, {
                scrollY: !0,
                scrollWithAnimation: !0,
                className: v,
                style: z,
                showScrollbar: !1,
                enhanced: !0,
                fastDeceleration: !0,
                enablePassive: !0,
                onTouchMove: function onTouchMove(e) {
                  e.preventDefault(), e.stopPropagation();
                },
                children: T,
              }),
              (k || M || I) &&
                (0, E.jsxs)(l.Ss, {
                  className: g,
                  children: [
                    (k || M) &&
                      (0, E.jsxs)(l.Ss, {
                        className: w,
                        children: [
                          k &&
                            (0, E.jsx)(l.$n, {
                              className: d()(b, x),
                              onClick: N || C,
                              children: k,
                            }),
                          M &&
                            (0, E.jsx)(
                              l.$n,
                              (0, r.A)(
                                (0, r.A)(
                                  {
                                    className: d()(y, x, (0, a.A)({}, A, D)),
                                    onClick: L,
                                    style: F,
                                  },
                                  W || {}
                                ),
                                {},
                                {
                                  children: M,
                                }
                              )
                            ),
                        ],
                      }),
                    I &&
                      (0, E.jsx)(l.Ss, {
                        className: _,
                        onClick: P,
                        style: V,
                        children: I,
                      }),
                  ],
                }),
            ],
          }),
        });
      };
    },
    9739: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return o;
        },
      });
      var i = n(7289),
        r = "index-module__mothlyImg___EBBTQ",
        a = n(4848),
        o = function MonthlyChallengTag() {
          return (0, a.jsx)(i._V, {
            src: "https://img.alicdn.com/imgextra/i2/O1CN01b52rml1oKOrV9yh7f_!!6000000005206-2-tps-200-70.png",
            mode: "scaleToFill",
            className: r,
          });
        };
    },
    7545: function (e, t, n) {
      "use strict";
      var i = n(9379),
        r = n(4462),
        a = n(6540),
        o = n(886),
        s = n(9299),
        l = n(4848);
      t.A = function View(e) {
        var t = e.onAppear,
          n = e.onFirstAppear,
          c = e.className,
          d = e.id,
          u = (0, a.useRef)(0),
          p = (0, a.useRef)(null);
        return (
          (0, o.A)(
            function () {
              var e;
              if (c || d) {
                var i =
                    null === (e = (0, s.wM)()) || void 0 === e
                      ? void 0
                      : e.join("."),
                  r = d ? "#".concat(d) : ".".concat(c),
                  a = document.querySelector(r),
                  o = new IntersectionObserver(function (e) {
                    e.forEach(function (e) {
                      var r,
                        a =
                          null === (r = (0, s.wM)()) || void 0 === r
                            ? void 0
                            : r.join(".");
                      i === a &&
                        e.isIntersecting &&
                        (t && t(), u.current++, u.current <= 1 && n && n());
                    });
                  });
                return (
                  setTimeout(function () {
                    a && o.observe(a);
                  }, 0),
                  function () {
                    p.current && (clearTimeout(p.current), (p.current = null)),
                      o && a && (o.unobserve(a), o.disconnect());
                  }
                );
              }
            },
            [c]
          ),
          (0, l.jsx)(r.A, (0, i.A)({}, e))
        );
      };
    },
    5019: function (e, t, n) {
      "use strict";
      n(5460), n(6185), n(6942), n(4848);
    },
    6669: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return s;
        },
      });
      var i = n(4462),
        r = n(4558),
        a = (n(6540), "index-module__maskHeader___lRqE1"),
        o = n(4848),
        s = function Mask() {
          (0, r.A)();
          return (0, o.jsx)(i.A, {
            className: a,
            style: {
              height: "56px",
            },
          });
        };
    },
    6453: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return SpmWrappedComponent;
        },
      });
      var i = n(9379),
        r = n(3986),
        a = n(6540),
        o = n(4462),
        s = n(9299),
        l = {
          page: "index-module__page___NPRww",
          wrapper: "index-module__wrapper___Eo2rI",
        },
        c = n(9975),
        d = n(5528),
        u = n(3940),
        p = n(6669),
        h = n(3272),
        m = n(7908),
        f = n(3696),
        v = n(6618),
        g = n(4848),
        w = [
          "outWrapperStyle",
          "bottomArea",
          "bottomWhite",
          "topArea",
          "header",
          "maskArea",
          "bottomAreaBgColor",
          "spmA",
          "spmB",
          "hideBar",
          "isHideShare",
          "isAlipayHideShare",
        ];
      function SpmWrappedComponent(e) {
        return function (t) {
          function SpmWrapper(n) {
            var b = e || {},
              y = b.outWrapperStyle,
              A = void 0 === y ? {} : y,
              x = b.bottomArea,
              _ = void 0 === x || x,
              E = b.bottomWhite,
              S = void 0 !== E && E,
              C = b.topArea,
              T = void 0 !== C && C,
              k = b.header,
              M = void 0 === k || k,
              I = b.maskArea,
              L = void 0 !== I && I,
              P = b.bottomAreaBgColor,
              N = b.spmA,
              O = b.spmB,
              D = b.hideBar,
              z = void 0 === D || D,
              j = b.isHideShare,
              B = void 0 === j || j,
              R = b.isAlipayHideShare,
              F = void 0 === R || R,
              Y = (0, r.A)(b, w),
              G = (0, d.A)().params.spm;
            return (
              (0, a.useEffect)(
                function () {
                  setTimeout(function () {
                    z && (0, h.uj)(), h.LA && (0, h.LA)();
                  }, 10);
                },
                [z]
              ),
              (0, a.useEffect)(
                function () {
                  if (B);
                  v.default.isAlipay &&
                    (F
                      ? my.postMessage({
                          name: "hideShareMenu",
                        })
                      : my.postMessage({
                          name: "showShareMenu",
                        }));
                },
                [B, F]
              ),
              (0, a.useEffect)(
                function () {
                  N && O && (0, s.cA)(N, O);
                },
                [N, O]
              ),
              (0, m.A)(
                function (e) {
                  !e &&
                    N &&
                    O &&
                    setTimeout(function () {
                      v.default.isAlipay &&
                        ((0, h.LA)(),
                        F
                          ? my.postMessage({
                              name: "hideShareMenu",
                            })
                          : my.postMessage({
                              name: "showShareMenu",
                            })),
                        v.default.isDingTalk && (0, h.uj)(),
                        (0, f.gu)((0, i.A)({}, (0, f._)(G || "")));
                    }, 700);
                },
                [N, O, F]
              ),
              (0, g.jsxs)(o.A, {
                className: l.page,
                children: [
                  T &&
                    (0, g.jsx)(c.A, {
                      position: "top",
                    }),
                  M && (0, g.jsx)(u.A, (0, i.A)({}, Y)),
                  (0, g.jsx)(o.A, {
                    className: l.wrapper,
                    style: (0, i.A)({}, A || {}),
                    children: (0, g.jsx)(t, (0, i.A)({}, n || {})),
                  }),
                  _ &&
                    (0, g.jsx)(c.A, {
                      position: "bottom",
                      style: {
                        background: S ? "#fff" : P || "var(--background)",
                      },
                    }),
                  L && (0, g.jsx)(p.A, {}),
                ],
              })
            );
          }
          return (SpmWrapper.displayName = e.spmB), SpmWrapper;
        };
      }
    },
    9975: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return f;
        },
      });
      var i = n(9379),
        r = n(4467),
        a = n(3986),
        o = n(4462),
        s = n(6942),
        l = n.n(s),
        c = n(6540),
        d = "index-module__safe-area____88vb",
        u = "index-module__safe-area--top___JCt4Y",
        p = "index-module__safe-area--bottom___vABOv",
        h = n(4848),
        m = ["className", "position"];
      var f = function SafeArea(e) {
        var t = e.className,
          n = void 0 === t ? "" : t,
          s = e.position,
          f = (0, a.A)(e, m),
          v = (0, c.useMemo)(
            function () {
              var e = l()(
                n,
                (0, r.A)((0, r.A)({}, u, "top" === s), p, "bottom" === s),
                d
              );
              return null != e ? e : {};
            },
            [s, n]
          );
        return (0, h.jsx)(
          o.A,
          (0, i.A)(
            {
              className: v,
            },
            f
          )
        );
      };
    },
    1634: function (e, t, n) {
      "use strict";
      n.d(t, {
        $U: function () {
          return r;
        },
        UG: function () {
          return a;
        },
        cd: function () {
          return s;
        },
        ip: function () {
          return o;
        },
        j9: function () {
          return i;
        },
      });
      var i =
          "https://gw.alicdn.com/imgextra/i3/O1CN01eHrqCR1Sl2FeOC5YC_!!6000000002286-49-tps-720-720.webp",
        r =
          "https://gw.alicdn.com/imgextra/i4/O1CN01EwVduo1lxNcuWOJRM_!!6000000004885-49-tps-720-720.webp",
        a =
          "https://gw.alicdn.com/imgextra/i4/O1CN015jPeRc1Hwyo7GWoiS_!!6000000000823-49-tps-720-720.webp",
        o =
          "https://gw.alicdn.com/imgextra/i1/O1CN01bEkjzK1xi3l8ZZdrh_!!6000000006476-49-tps-96-96.webp",
        s =
          "https://img.alicdn.com/imgextra/i3/O1CN01dYGojp1ZCs16ez9Mm_!!6000000003159-0-tps-720-720.jpg";
    },
    7160: function (e, t, n) {
      "use strict";
    },
    5528: function (e, t, n) {
      "use strict";
      var i = n(4025),
        r = n(813);
      t.A = function useCustomRouter() {
        var e = (i.Ay.useRouter() || {}).params,
          t = (0, r.HM)(e) || {};
        for (var n in t)
          ("undefined" !== t[n] && "null" !== t[n]) || (t[n] = "");
        return {
          params: t,
        };
      };
    },
    4558: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return h;
        },
      });
      var i = n(9379),
        r = n(675),
        a = n(467),
        o = n(5544),
        s = n(1472),
        l = n(4025),
        c = n(6540),
        d = n(6618),
        u = n(4494),
        p = null;
      var h = function useGetHeaderData() {
        var e,
          t,
          n = (function useGetDefaultHeaderData() {
            return (0, c.useMemo)(function () {
              var e = (0, s.j5)(),
                t = !1;
              null != e &&
                e.system &&
                (t = (e || {}).system.indexOf("iOS") > -1);
              var n =
                t &&
                -1 !==
                  [693, 812, 844, 896, 926, 1218, 1344, 852, 932].indexOf(
                    null == e ? void 0 : e.screenHeight
                  );
              return d.default.isAlipay
                ? n && t
                  ? {
                      statusBarHeight: 44,
                    }
                  : {
                      statusBarHeight: 48,
                    }
                : d.default.isLedongli || d.default.isDingTalk
                ? n
                  ? {
                      statusBarHeight: 44,
                      top: 44,
                      bottom: 88,
                    }
                  : t
                  ? {
                      statusBarHeight: 20,
                      top: 44,
                      bottom: 64,
                    }
                  : d.isMiniAppWebView
                  ? {
                      statusBarHeight: 40,
                      top: 48,
                      bottom: 88,
                    }
                  : {
                      statusBarHeight: 0,
                      top: 48,
                      bottom: 48,
                    }
                : {};
            }, []);
          })(),
          h = (0, c.useState)(n),
          m = (0, o.A)(h, 2),
          f = m[0],
          v = m[1];
        (0, c.useEffect)(function () {
          d.default.isAlipay &&
            (!(function listenMybegin() {
              my.postMessage({
                name: "getHeaderData",
              });
            })(),
            (my.onMessage = function (e) {
              "getHeaderData" === e.message && e.data && (p = e.data);
            }));
        }, []);
        var g = (0, u.q)(
          (0, a.A)(
            (0, r.A)().mark(function _callee() {
              var e, t, n, i, a, o, c, u, h, m, f, g, w, b, y, A, x;
              return (0, r.A)().wrap(
                function _callee$(r) {
                  for (;;)
                    switch ((r.prev = r.next)) {
                      case 0:
                        (n = (0, s.j5)()),
                          (i = d.default.isAlipay
                            ? null === (e = p) || void 0 === e
                              ? void 0
                              : e.bottom
                            : 56),
                          (a = d.default.isAlipay
                            ? null === (t = p) || void 0 === t
                              ? void 0
                              : t.top
                            : 56),
                          (o = null == n ? void 0 : n.screenWidth),
                          (c = o / 750),
                          (u = !1),
                          (h = 0),
                          null != n &&
                            n.system &&
                            ((m = (n || {}).system),
                            (u = m.indexOf("iOS") > -1)),
                          (f = u ? 88 : 96),
                          (r.next = 16);
                        break;
                      case 16:
                        if (!d.default.isAlipay) {
                          r.next = 31;
                          break;
                        }
                        return (
                          (r.prev = 17),
                          (r.next = 20),
                          null === (g = window) ||
                          void 0 === g ||
                          null === (g = g.AliJSBridge) ||
                          void 0 === g
                            ? void 0
                            : g.callClientBridge("getTitleAndStatusbarHeight")
                        );
                      case 20:
                        (A = r.sent),
                          (h =
                            (null == A ||
                            null === (w = A.output) ||
                            void 0 === w
                              ? void 0
                              : w.statusBarHeight) || 48),
                          (a =
                            (null === (b = p) || void 0 === b
                              ? void 0
                              : b.top) || 56),
                          (i =
                            (null === (y = p) || void 0 === y
                              ? void 0
                              : y.bottom) || 56),
                          (r.next = 29);
                        break;
                      case 26:
                        (r.prev = 26),
                          (r.t0 = r.catch(17)),
                          console.log(
                            "[as-page-navbar]-jsapi-error",
                            null === r.t0 || void 0 === r.t0
                              ? void 0
                              : r.t0.message
                          );
                      case 29:
                        r.next = 32;
                        break;
                      case 31:
                        (d.default.isLedongli || d.default.isDingTalk) &&
                          ((x = [
                            693, 812, 844, 896, 926, 1218, 1344, 852, 932,
                          ]),
                          u &&
                          -1 !== x.indexOf(null == n ? void 0 : n.screenHeight)
                            ? ((h = 44), (a = 44), (i = 88))
                            : u
                            ? ((h = 20), (a = 44), (i = 64))
                            : d.isMiniAppWebView
                            ? ((h = 40), (a = 48), (i = 88))
                            : ((h = 0), (a = 48), (i = 48)));
                      case 32:
                        v({
                          boxHeight: h + f,
                          statusBarHeight: h,
                          getHeight: (0, l.n_)(f),
                          factor: c,
                          bottom: i,
                          top: a,
                        });
                      case 34:
                      case "end":
                        return r.stop();
                    }
                },
                _callee,
                null,
                [[17, 26]]
              );
            })
          )
        );
        return (
          (0, c.useEffect)(
            function () {
              g();
            },
            [g]
          ),
          (0, i.A)(
            (0, i.A)({}, f || {}),
            {},
            {
              top: (null === (e = p) || void 0 === e ? void 0 : e.top) || f.top,
              bottom:
                (null === (t = p) || void 0 === t ? void 0 : t.bottom) ||
                f.bottom,
              buttonHeight: d.isDingTalk
                ? ((null == f ? void 0 : f.statusBarHeight) || 0) + 10
                : null == f
                ? void 0
                : f.statusBarHeight,
            }
          )
        );
      };
    },
    4494: function (e, t, n) {
      "use strict";
      n.d(t, {
        q: function () {
          return useMemoizedFn;
        },
      });
      var i = n(6540);
      function useMemoizedFn(e) {
        var t = (0, i.useRef)(e);
        t.current = (0, i.useMemo)(
          function () {
            return e;
          },
          [e]
        );
        var n = (0, i.useRef)();
        return (
          n.current ||
            (n.current = function () {
              for (
                var e = arguments.length, n = new Array(e), i = 0;
                i < e;
                i++
              )
                n[i] = arguments[i];
              return t.current.apply(this, n);
            }),
          n.current
        );
      }
    },
    7908: function (e, t, n) {
      "use strict";
      n.d(t, {
        A: function () {
          return l;
        },
      });
      var i = n(4025),
        r = n(6540),
        a = n(6618),
        o = n(886),
        s = function usePageDidShow(e) {
          var t =
              arguments.length > 1 && void 0 !== arguments[1]
                ? arguments[1]
                : [],
            n = (0, r.useRef)(e),
            s = (0, r.useRef)(e);
          (0, r.useEffect)(
            function () {
              (n.current = e), (s.current = e);
            },
            [e]
          ),
            i.Ay.useDidShow(function () {
              (n.current = s.current), n.current && n.current(!1);
            }),
            i.Ay.useDidHide(function () {
              n.current && n.current(!0), (n.current = null);
            }),
            i.Ay.useUnload(function () {
              (n.current = e), (s.current = e);
            }),
            (0, o.A)(function () {
              var e = (function getResumeEventInfo() {
                  var e, t;
                  return (
                    void 0 !== document.hidden
                      ? ((e = "hidden"), (t = "visibilitychange"))
                      : void 0 !== document.msHidden
                      ? ((e = "msHidden"), (t = "msvisibilitychange"))
                      : void 0 !== document.webkitHidden &&
                        ((e = "webkitHidden"), (t = "webkitvisibilitychange")),
                    {
                      hidden: e,
                      visibilityChange: t,
                    }
                  );
                })(),
                t = e.visibilityChange,
                i = e.hidden,
                r = function handleVisibleChange() {
                  if (window.is_ssu_vip) {
                    window.ssu_setTitle("跳过handleVisibleChange1");
                    return;
                  }
                  n.current && n.current(document[i]),
                    n.current &&
                      "function" == typeof n.current &&
                      !document[i] &&
                      a.default.isLedongli &&
                      (function callLogin() {
                        window.WindVane.call(
                          "LeWVJSBridge",
                          "getUserInfo",
                          {},
                          function (e) {
                            0 == (e || {}).isLogin && window.location.reload();
                          },
                          function (e) {
                            window.location.reload();
                          }
                        );
                      })();
                };
              return (
                document.addEventListener(t, r, !1),
                function () {
                  document.removeEventListener(t, r, !1);
                }
              );
            }, t);
        },
        l = s;
    },
    886: function (e, t, n) {
      "use strict";
      var i = n(6540);
      t.A = function useSafeEffect(e, t) {
        (0, i.useEffect)(function () {
          var t = !1,
            n = e(!t);
          return function () {
            var e = function recursion(t) {
              t instanceof Promise
                ? t.then(function (t) {
                    e(t);
                  })
                : t instanceof Function && t();
            };
            e(n), (t = !0);
          };
        }, t);
      };
    },
    9367: function (e, t, n) {
      "use strict";
      n.r(t),
        n.d(t, {
          default: function () {
            return Yn;
          },
        });
      var i = n(4467),
        r = n(4765),
        a = n(436),
        o = n(675),
        s = n(467),
        l = n(2284),
        c = n(9379),
        d = n(5544),
        u = n(6453),
        p = n(9958),
        h = n(1072),
        m = n(5528),
        f = n(6942),
        v = n.n(f),
        g = n(4462),
        w = "index-module__cellContent___zwdG0",
        b = n(7289),
        y = {
          gymSelectItem: "index-module__gymSelectItem___Uw1OH",
          gymItem: "index-module__gymItem___LZBew",
          isSelectItem: "index-module__isSelectItem___La9NL",
          selectImg: "index-module__selectImg___qDDom",
        },
        A = (function (e) {
          return (
            (e.LIMITEDTIME = "LIMITED_TIME"),
            (e.SUPERMEMBER = "SUPER_MEMBER"),
            e
          );
        })({}),
        x = (function (e) {
          return (
            (e.AVALIABLE_FOR_SALE = "AVALIABLE_FOR_SALE"),
            (e.NO_AVALIABLE_FOR_SALE = "NO_AVALIABLE_FOR_SALE"),
            (e.N0_COMMMODITY_CONFIG = "N0_COMMMODITY_CONFIG"),
            e
          );
        })({}),
        _ = n(4025),
        E = [
          {
            color: "#D4D4D4",
            id: 0,
            bgColor: "#F8F8F8",
            brColor: "#666",
            selectedIcon:
              "https://gw.alicdn.com/imgextra/i2/O1CN01VjisiO1SO8UYKsAFU_!!6000000002236-49-tps-47-46.webp",
          },
          {
            color: "#90C0F7",
            id: 1,
            bgColor: "#F0F7FF",
            brColor: "#157BF9",
            selectedIcon:
              "https://gw.alicdn.com/imgextra/i1/O1CN01SBqHej1rPNxzzLpcL_!!6000000005623-49-tps-47-46.webp",
          },
          {
            color: "#FFE682",
            id: 2,
            bgColor: "#FFFBE9",
            brColor: "#E9B213",
            selectedIcon:
              "https://gw.alicdn.com/imgextra/i1/O1CN01GqQ0Ld1CPwCDau2r8_!!6000000000074-49-tps-47-46.webp",
          },
          {
            color: "#F0C3A2",
            id: 3,
            bgColor: "#FFF3EA",
            brColor: "#D57241",
            selectedIcon:
              "https://gw.alicdn.com/imgextra/i4/O1CN01GV9ktk1msdBAgo5BY_!!6000000005010-49-tps-47-46.webp",
          },
          {
            color: "#74E0A8",
            id: 4,
            bgColor: "#EAFFF4",
            brColor: "#0BBD60",
            selectedIcon:
              "https://gw.alicdn.com/imgextra/i2/O1CN01eGz0Di1IzYhEM6JG2_!!6000000000964-49-tps-47-46.webp",
          },
        ],
        S = n(4353),
        C = n.n(S),
        T = n(1472),
        k =
          "https://gw.alicdn.com/imgextra/i4/O1CN01ZqrDZV1SMIuQvK8gS_!!6000000002232-49-tps-54-52.webp",
        M = function getOffsetLeft(e) {
          var t = (0, T.j5)().windowWidth;
          return Math.round((t / 750) * e);
        },
        I = function uniqueItem(e) {
          var t,
            n = new Map(),
            i = [],
            a = (0, r.A)(e);
          try {
            for (a.s(); !(t = a.n()).done; ) {
              var o = t.value,
                s = (o || {}).commodityId;
              s && !n.has(s) && (n.set(s, !0), i.push(o));
            }
          } catch (e) {
            a.e(e);
          } finally {
            a.f();
          }
          return i;
        },
        L = function timeBetweenStartAndEnd(e, t, n) {
          var i = C()()
              .set("hour", Number(e.split(":")[0]))
              .set("minute", Number(e.split(":")[1])),
            r = C()()
              .set("hour", Number(t.split(":")[0]))
              .set("minute", Number(t.split(":")[1])),
            a = C()()
              .set("hour", Number(n.split(":")[0]))
              .set("minute", Number(n.split(":")[1]));
          return i >= r && i < a;
        },
        P = function getPxTransform(e) {
          return e.replace(/(^[\d.]+)(.+)$/, function (e, t, n) {
            return Number(t).toFixed(1) + n;
          });
        },
        N = function handleTransformsubtract(e, t) {
          var n = e.replace(/(^[\d.]+)(.+)$/, function (e, t, n) {
              return n;
            }),
            i = t.replace(/(^[\d.]+)(.+)$/, function (e, t, n) {
              return Number(t).toFixed(1);
            });
          return (
            e.replace(/(^[\d.]+)(.+)$/, function (e, t, n) {
              return Number(t).toFixed(1);
            }) -
            i +
            n
          );
        },
        O = (P((0, _.n_)(76)), P((0, _.n_)(8)), P((0, _.n_)(84))),
        D = P((0, _.n_)(17)),
        z = P((0, _.n_)(8)),
        j = P((0, _.n_)(8)),
        B = P((0, _.n_)(20)),
        R =
          (P((0, _.n_)(16)),
          M(Number((0, _.n_)(76).replace("rpx", ""))),
          M(Number((0, _.n_)(8).replace("rpx", ""))),
          P((0, _.n_)(20)),
          P((0, _.n_)(8))),
        F = function timeItemLineHeight() {
          return O;
        },
        Y = function pageContentScrollViewTimeMT(e) {
          return (function transFormEndData(e) {
            return P((0, _.n_)(1 * e));
          })(e);
        },
        G = n(4171),
        V = n(6540),
        W = n(4848);
      var H = function GymCard(e) {
          var t = e.onSelect,
            n = e.item,
            r = e.isSelect,
            a = void 0 !== r && r,
            o = e.name,
            s = e.mergedCells,
            l = void 0 === s ? 1 : s,
            d = (0, V.useMemo)(
              function () {
                var e = N((0, _.n_)(84 * l), (0, _.n_)(8));
                return "".concat(e);
              },
              [l]
            ),
            u = (0, V.useMemo)(
              function () {
                var e,
                  t,
                  i,
                  r,
                  a,
                  o,
                  s = (n || {}).skuIndex || 0;
                return s > 4
                  ? {
                      bgColor:
                        null === (r = E[0]) || void 0 === r
                          ? void 0
                          : r.bgColor,
                      brColor:
                        null === (a = E[0]) || void 0 === a
                          ? void 0
                          : a.brColor,
                      selectedIcon:
                        null === (o = E[0]) || void 0 === o
                          ? void 0
                          : o.selectedIcon,
                    }
                  : {
                      bgColor:
                        null === (e = E[s]) || void 0 === e
                          ? void 0
                          : e.bgColor,
                      brColor:
                        null === (t = E[s]) || void 0 === t
                          ? void 0
                          : t.brColor,
                      selectedIcon:
                        null === (i = E[s]) || void 0 === i
                          ? void 0
                          : i.selectedIcon,
                    };
              },
              [n]
            );
          return n
            ? (0, W.jsx)(g.A, {
                className: v()(
                  (0, i.A)((0, i.A)({}, y.gymItem, !0), y.isSelectItem, a)
                ),
                style: {
                  height: d,
                  marginTop: "".concat(R),
                  backgroundColor: u.bgColor || u.bgColor,
                  borderColor: u.brColor,
                  color: u.brColor || u.brColor,
                },
                onClick: function onClick() {
                  return (function handleSelectItem(e) {
                    t &&
                      t(
                        (0, c.A)(
                          (0, c.A)({}, e),
                          {},
                          {
                            name: o,
                          }
                        )
                      );
                  })(n);
                },
                children: (function emptyRender() {
                  return ((null == n ? void 0 : n.inSell) ===
                    x.NO_AVALIABLE_FOR_SALE && !window.is_ssu_vip
                    ? "未开售"
                    : null == n
                    ? void 0
                    : n.inSell) === x.N0_COMMMODITY_CONFIG
                    ? "不可售"
                    : void 0 === (null == n ? void 0 : n.originPrice) ||
                      null === (null == n ? void 0 : n.originPrice) ||
                      "" === String(n.originPrice)
                    ? "已售"
                    : (0, W.jsxs)(W.Fragment, {
                        children: [
                          a
                            ? (0, W.jsx)(b._V, {
                                className: y.selectImg,
                                webp: !0,
                                mode: "aspectFill",
                                src: u.selectedIcon,
                              })
                            : null,
                          (0, W.jsx)(b.EY, {
                            className: y.unit,
                            children: "¥",
                          }),
                          (0, W.jsx)(b.EY, {
                            children: (0, G.F)(n.originPrice),
                          }),
                        ],
                      });
                })(),
              })
            : null;
        },
        X = "index-module__gymSelectItem___eQgYD";
      var U = function CantOrderCard(e) {
          var t = e.mergedCells,
            n = e.item,
            i = (0, V.useMemo)(
              function () {
                var e = N((0, _.n_)(84 * t), (0, _.n_)(8));
                return "".concat(e);
              },
              [t]
            );
          return (0, W.jsx)(g.A, {
            className: v()(X),
            style: {
              height: i,
              marginTop: "".concat(R),
            },
            children: (0, W.jsx)(b.EY, {
              children:
                (null == n ? void 0 : n.inSell) === x.NO_AVALIABLE_FOR_SALE &&
                !window.is_ssu_vip
                  ? "未开售"
                  : (null == n ? void 0 : n.inSell) === x.N0_COMMMODITY_CONFIG
                  ? "不可售"
                  : (null == n ? void 0 : n.inSell) === x.AVALIABLE_FOR_SALE
                  ? "已售"
                  : null,
            }),
          });
        },
        Z = {
          gymSelectItem: "index-module__gymSelectItem___kj8MA",
        };
      var Q = function ExpiredCard(e) {
          var t = e.item,
            n = e.mergedCells,
            i = (0, V.useMemo)(
              function () {
                var e = N((0, _.n_)(84 * n), (0, _.n_)(8));
                return "".concat(e);
              },
              [n]
            );
          return t
            ? (0, W.jsx)(g.A, {
                onClick: function handleClick() {
                  (0, p.P0)({
                    title: "该时间段已过期",
                    icon: "none",
                    duration: 2e3,
                  });
                },
                className: v()(Z.gymSelectItem),
                style: {
                  height: i,
                  marginTop: "".concat(R),
                },
                children: (function emptyRender() {
                  return ((null == t ? void 0 : t.inSell) ===
                    x.NO_AVALIABLE_FOR_SALE && !window.is_ssu_vip
                    ? "未开售"
                    : null == t
                    ? void 0
                    : t.inSell) === x.N0_COMMMODITY_CONFIG
                    ? "不可售"
                    : void 0 === (null == t ? void 0 : t.originPrice) ||
                      null === (null == t ? void 0 : t.originPrice) ||
                      "" === String(t.originPrice)
                    ? "已售"
                    : (0, W.jsxs)(W.Fragment, {
                        children: [
                          (0, W.jsx)(b.EY, {
                            className: Z.unit,
                            children: "¥",
                          }),
                          (0, W.jsx)(b.EY, {
                            children: (0, G.F)(t.originPrice),
                          }),
                        ],
                      });
                })(),
              })
            : null;
        },
        q = "index-module__gymSelectItem___d07_v";
      var J = function NotAvailAbleCard(e) {
        var t = e.mergedCells,
          n =
            (e.item,
            (0, V.useMemo)(
              function () {
                var e = N((0, _.n_)(84 * t), (0, _.n_)(8));
                return "".concat(e);
              },
              [t]
            ));
        return (0, W.jsx)(g.A, {
          className: v()(q),
          style: {
            height: n,
            marginTop: "".concat(R),
          },
          children: (0, W.jsx)(b.EY, {
            children: "不可售",
          }),
        });
      };
      function GymList(e) {
        var t = e.onSelect,
          n = e.selectList,
          i = e.List,
          r = e.onRemove,
          a = (0, V.useMemo)(
            function () {
              var e = {};
              return (
                Array.isArray(n) &&
                  n.forEach(function (t, n) {
                    var i = t.reserveId;
                    e[i] = n;
                  }),
                e
              );
            },
            [n]
          ),
          o = function removeItem(e) {
            if (a) {
              var t = a[e];
              t > -1 && r && r(t);
            }
          };
        return i
          ? (0, W.jsx)(W.Fragment, {
              children:
                null == i
                  ? void 0
                  : i
                      .filter(function (e) {
                        var t;
                        return null === (t = e.rowList) || void 0 === t
                          ? void 0
                          : t.length;
                      })
                      .map(function (e, i) {
                        var r;
                        return (0, W.jsx)(
                          g.A,
                          {
                            className: w,
                            children:
                              null == e ||
                              null === (r = e.rowList) ||
                              void 0 === r
                                ? void 0
                                : r.map(function (r, a) {
                                    var s = r || {},
                                      l = s.overdue,
                                      c = s.inventory,
                                      d = s.mergedCells,
                                      u = s.reserveId;
                                    if (s.inSell === x.N0_COMMMODITY_CONFIG)
                                      return (0, W.jsx)(
                                        J,
                                        {
                                          item: r,
                                          mergedCells: d,
                                          onSelect: t,
                                        },
                                        u || "".concat(i, "-").concat(a)
                                      );
                                    if (l)
                                      return (
                                        o(u),
                                        (0, W.jsx)(
                                          Q,
                                          {
                                            item: r,
                                            mergedCells: d,
                                            onSelect: t,
                                          },
                                          u || "".concat(i, "-").concat(a)
                                        )
                                      );
                                    if (c && u) {
                                      var p = !(
                                        null == n ||
                                        !n.filter(function (e) {
                                          return e.reserveId === r.reserveId;
                                        }).length
                                      );
                                      return void 0 ===
                                        (null == r ? void 0 : r.price) ||
                                        null ===
                                          (null == r ? void 0 : r.price) ||
                                        "" === String(r.price) ||
                                        (null == r ? void 0 : r.inSell) ===
                                          x.N0_COMMMODITY_CONFIG
                                        ? //   ||
                                          // (null == r ? void 0 : r.inSell) === x.NO_AVALIABLE_FOR_SALE && !window.is_ssu_vip
                                          (o(u),
                                          (0, W.jsx)(
                                            U,
                                            {
                                              onSelect: t,
                                              item: r,
                                              mergedCells: d,
                                            },
                                            u || "".concat(i, "-").concat(a)
                                          ))
                                        : (0, W.jsx)(
                                            H,
                                            {
                                              onSelect: t,
                                              item: r,
                                              isSelect: p,
                                              name: e.name,
                                              mergedCells: d,
                                            },
                                            u || "".concat(i, "-").concat(a)
                                          );
                                    }
                                    return (
                                      o(u),
                                      (0, W.jsx)(
                                        U,
                                        {
                                          mergedCells: d,
                                          item: r,
                                          onSelect: t,
                                        },
                                        u || "".concat(i, "-").concat(a)
                                      )
                                    );
                                  }),
                          },
                          i
                        );
                      }),
            })
          : (0, W.jsx)(g.A, {
              children: "不存在",
            });
      }
      var K = (0, V.memo)(GymList),
        ee = n(886),
        te = n(1634),
        ne = "index-module__backRequest___QMgJ3",
        ie = "index-module__emptyImg____yFO7",
        re = "index-module__emptyText___ktGUh",
        ae = n(3696),
        oe = n(5480);
      var se = function EmptyContent(e) {
          var t = e.stadiumId,
            n = e.propertyValueId;
          return (
            (0, V.useEffect)(
              function () {
                (0, ae.FB)({
                  type: oe.T.JS_ERROR,
                  message: "场地预定,导致页面请求数据失败",
                  error: "场馆数据为".concat(t, "，商品数据为").concat(n),
                });
              },
              [n, t]
            ),
            (0, W.jsxs)(b.Ss, {
              className: ne,
              children: [
                (0, W.jsx)(b._V, {
                  className: ie,
                  webp: !0,
                  mode: "aspectFill",
                  src: te.$U,
                }),
                (0, W.jsx)(b.EY, {
                  className: re,
                  children: "请求数据失败",
                }),
              ],
            })
          );
        },
        le = function useGetDate() {
          return (0, V.useMemo)(function () {
            for (
              var e = C()(),
                t = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"],
                n = [],
                i = 0;
              i < 7;
              i++
            ) {
              var r = e.add(i, "day");
              n.push({
                num: r.format("DD"),
                month: r.format("MM"),
                year: r.format("YYYY"),
                text: t[r.day()],
                limitTimeTag: !1,
              });
            }
            return {
              current: e.format("YYYY-MM-DD"),
              currentMonth: e.format("MM"),
              currentYear: e.format("YYYY"),
              today: e.format("DD"),
              weekList: n,
            };
          }, []);
        },
        ce = "index-module__selectTime___ZpFl0",
        de = "index-module__timeList___F6aIt",
        ue = "index-module__weekList___xO1yg",
        pe = "index-module__weekItem___FePki",
        he = "index-module__selectWeekItem___XWP7G",
        me = "index-module__weekItemText___MW_qr",
        fe = "index-module__weekItemNum___bJoiE",
        ve = "index-module__discountLabelContent___Hvo9h",
        ge = "index-module__textDecorate___EgiXJ";
      var we = function DiscountLabel(e) {
          var t = e.outStyle,
            n = void 0 === t ? {} : t;
          return (0, W.jsx)(b.Ss, {
            className: ve,
            style: n,
            children: (0, W.jsx)(b.EY, {
              className: ge,
              children: "惠",
            }),
          });
        },
        be = "index-module__disabledOrderContainer___PRdef",
        ye = "index-module__textDecorate___hnn7c";
      var Ae = function DisabledOrder(e) {
        var t = e.outStyle,
          n = void 0 === t ? {} : t;
        return (0, W.jsx)(b.Ss, {
          className: be,
          style: n,
          children: (0, W.jsx)(b.EY, {
            className: ye,
            children: "无可订",
          }),
        });
      };
      function TimeList(e) {
        var t = e.currentDate,
          n = e.onClickWeekItem,
          r = e.selectDay,
          a = e.TimeListId,
          o = (t || {}).weekList,
          s = void 0 === o ? [] : o,
          l = function renderFlag(e) {
            if (e.setWeekList) {
              if (!e.canReserve) return (0, W.jsx)(Ae, {});
              if (e.discountTag)
                return (0, W.jsx)(we, {
                  outStyle: {
                    right: 0,
                    top: 0,
                  },
                });
            }
            return null;
          };
        return (0, W.jsx)(
          b.Ss,
          {
            className: ce,
            children: (0, W.jsx)(b.BM, {
              scrollX: !0,
              enableFlex: !0,
              showScrollbar: !1,
              enhanced: !0,
              className: de,
              scrollWithAnimation: !0,
              fastDeceleration: !0,
              scrollIntoView: a,
              children: (0, W.jsx)(b.Ss, {
                className: ue,
                children: s.map(function (e) {
                  return (0, W.jsxs)(
                    b.Ss,
                    {
                      className: pe,
                      id: "ID-"
                        .concat(e.num, "-")
                        .concat(e.month, "-")
                        .concat(e.year),
                      onClick: function onClick() {
                        return n(e.num, e.month, e.year);
                      },
                      children: [
                        l(e),
                        (0, W.jsxs)(b.Ss, {
                          className: v()(
                            (0, i.A)(
                              {},
                              he,
                              r
                                ? (null == r ? void 0 : r.num) === e.num
                                : t.today === e.num
                            )
                          ),
                          children: [
                            (0, W.jsx)(b.Ss, {
                              className: me,
                              children: t.today === e.num ? "今日" : e.text,
                            }),
                            (0, W.jsx)(b.Ss, {
                              className: fe,
                              children: "".concat(e.month, ".").concat(e.num),
                            }),
                          ],
                        }),
                      ],
                    },
                    e.num
                  );
                }),
              }),
            }),
          },
          "selectTimeDom"
        );
      }
      var xe = (0, V.memo)(TimeList),
        _e = n(7908),
        Ee = "index-module__seletItemContent___vBl1A",
        Se = "index-module__header___i29rJ",
        Ce = "index-module__closeIcon___x4y3G",
        Te = "index-module__content___aEDeH",
        ke = "index-module__seletItemFee___WtdvK",
        Me = "index-module__price___wTcUl",
        Ie = "index-module__originPrice___rTUyE",
        Le = "index-module__discountcontent___XD4Q6",
        Pe = n(3230),
        Ne = n(97);
      function parseLimitActivityText(e) {
        var t = e.platformMarketingDetail;
        if (null == t || !t.discountType) return [];
        var n = t || {},
          i = n.discountType,
          r = n.discountValue,
          a = "";
        if (r > 0)
          switch (i) {
            case Ne.ui.PERCENT:
              a = "".concat((0, G.H8)(r), "折");
              break;
            case Ne.ui.REDUCE:
              a = "减".concat((0, G.F)(r), "元");
              break;
            default:
              console.log("discountType --- error", i);
          }
        return a
          ? (function addMark(e) {
              var t,
                n = e.type,
                i = e.data;
              return (
                (null == i ||
                null ===
                  (t = i.filter(function (e) {
                    return !!e;
                  })) ||
                void 0 === t
                  ? void 0
                  : t.map(function (e) {
                      return {
                        type: n,
                        data: e || "",
                      };
                    })) || []
              );
            })({
              type: Pe.Q.LIMITED_DISCOUNT,
              data: [a],
            })
          : [];
      }
      var Oe = "index-module__discountImg___nUbH0";
      var De = function VipMember() {
          return (0, W.jsx)(b._V, {
            className: Oe,
            webp: !0,
            mode: "aspectFill",
            src: "https://gw.alicdn.com/imgextra/i3/O1CN01guDyLf1bORINYNJQz_!!6000000003455-49-tps-148-52.webp",
          });
        },
        ze = "index-module__discountLabelContent___QmH6j",
        je = "index-module__textDecorate___y26v8";
      var Be = function DiscountContent(e) {
        var t = e.outStyle,
          n = void 0 === t ? {} : t,
          i = e.discountText,
          r = e.couponType;
        return r !== A.LIMITEDTIME && r !== A.SUPERMEMBER
          ? null
          : r === A.SUPERMEMBER
          ? (0, W.jsx)(De, {})
          : i
          ? (0, W.jsx)(b.Ss, {
              className: ze,
              style: n,
              children: (0, W.jsx)(b.EY, {
                className: je,
                children: i,
              }),
            })
          : null;
      };
      var Re = function SeletItem(e) {
          var t = e.item,
            n = e.titleDay,
            i = e.onSelect,
            r = e.selectList,
            a = (0, V.useMemo)(
              function () {
                var e,
                  n = (t || {}).discountInfo || {},
                  i = n.couponType,
                  a = n.discountValue,
                  o = n.calculateType;
                if (
                  (null === (e = I(r)) || void 0 === e ? void 0 : e.length) <= 1
                )
                  return {
                    discountText: "",
                    couponType: A.LIMITEDTIME,
                  };
                if (i === A.SUPERMEMBER)
                  return {
                    discountText: "",
                    couponType: A.SUPERMEMBER,
                  };
                if (i === A.LIMITEDTIME) {
                  var s,
                    l = parseLimitActivityText({
                      platformMarketingDetail: {
                        discountType: o,
                        discountValue: a,
                      },
                    });
                  return {
                    discountText:
                      null == l || null === (s = l[0]) || void 0 === s
                        ? void 0
                        : s.data,
                    couponType: i,
                  };
                }
                return {
                  discountText: "",
                  couponType: A.LIMITEDTIME,
                };
              },
              [t, r]
            ),
            o = (0, V.useMemo)(
              function () {
                var e, n;
                if (t.name)
                  return (null == t || null === (e = t.name) || void 0 === e
                    ? void 0
                    : e.length) > 7
                    ? "".concat(
                        null == t || null === (n = t.name) || void 0 === n
                          ? void 0
                          : n.slice(0, 6),
                        "..."
                      )
                    : null == t
                    ? void 0
                    : t.name;
              },
              [t.name]
            );
          return (0, W.jsxs)(g.A, {
            className: Ee,
            children: [
              (0, W.jsxs)(g.A, {
                className: Se,
                children: [
                  o,
                  (0, W.jsx)(b._V, {
                    className: Ce,
                    onClick: function handleRemove() {
                      i && i((0, c.A)({}, t));
                    },
                    webp: !0,
                    mode: "aspectFill",
                    src: "https://gw.alicdn.com/imgextra/i3/O1CN010m43EQ1YEPmsTnM3C_!!6000000003027-2-tps-48-48.png",
                  }),
                ],
              }),
              (0, W.jsxs)(g.A, {
                className: Te,
                children: [n, " ", t.startTime, "-", t.endTime],
              }),
              (0, W.jsxs)(g.A, {
                className: ke,
                children: [
                  void 0 !== t.discountedPrice
                    ? (0, W.jsxs)(b.EY, {
                        className: Me,
                        children: ["¥", (0, G.F)(t.discountedPrice)],
                      })
                    : null,
                  void 0 !== t.discountedPrice &&
                  t.originPrice &&
                  t.discountedPrice !== t.originPrice
                    ? (0, W.jsxs)(b.EY, {
                        className: Ie,
                        children: ["¥", (0, G.F)(t.originPrice)],
                      })
                    : null,
                  a.couponType === A.SUPERMEMBER ||
                  (a.couponType === A.LIMITEDTIME && a.discountText)
                    ? (0, W.jsx)(g.A, {
                        className: Le,
                        children: (0, W.jsx)(Be, (0, c.A)({}, a)),
                      })
                    : null,
                ],
              }),
            ],
          });
        },
        Fe = "index-module__orderGymNumNoCollasp___Pqnd7",
        $e = "index-module__orderGymNumCollasp___SHKiG",
        Ye = "index-module__orderGymNum___dSP7H",
        Ge = "index-module__header___H5Obx",
        Ve = "index-module__selectNum___HQvdS",
        We = "index-module__collasp___F1AoL",
        He = "index-module__collaspImg___POkbb",
        Xe = "index-module__selectContent___TvXvc",
        Ue = "index-module__orderGymContent___FDpUt";
      var Ze = function CustomSelectList(e) {
          var t = e.selectList,
            n = e.isCollasp,
            r = e.onSetCollasp,
            a = e.selectDay,
            o = e.renderSelectList,
            s = void 0 === o ? [] : o,
            l = e.onSelect,
            c = (0, V.useMemo)(
              function () {
                var e = !!t.length;
                return {
                  opacity: e ? 1 : 0,
                  visibility: e ? "visible" : "hidden",
                  position: e ? "relative" : "absolute",
                  height: e ? "128px" : 0,
                  zIndex: 79,
                };
              },
              [t]
            ),
            d = (0, V.useMemo)(
              function () {
                return t.length;
              },
              [t]
            );
          return (0, W.jsxs)(b.Ss, {
            style: c,
            className: v()(
              (0, i.A)(
                (0, i.A)((0, i.A)({}, Ye, !!d), Fe, d && !n),
                $e,
                (null == t ? void 0 : t.length) && n
              )
            ),
            children: [
              (0, W.jsxs)(b.Ss, {
                className: Ge,
                children: [
                  (0, W.jsxs)(b.Ss, {
                    children: [
                      "已选场次 ",
                      (0, W.jsx)(b.EY, {
                        className: Ve,
                        children: t.length,
                      }),
                    ],
                  }),
                  t.length > 3
                    ? (0, W.jsxs)(b.Ss, {
                        className: We,
                        onClick: r,
                        children: [
                          "全部",
                          (0, W.jsx)(b._V, {
                            className: He,
                            webp: !0,
                            mode: "aspectFill",
                            src: n
                              ? "https://gw.alicdn.com/imgextra/i4/O1CN01ndxdfv1s4bZD2pvix_!!6000000005713-49-tps-60-60.webp"
                              : "https://gw.alicdn.com/imgextra/i1/O1CN01zsYSYD22AgzGf7Y9M_!!6000000007080-49-tps-60-60.webp",
                          }),
                        ],
                      })
                    : null,
                ],
              }),
              null != t && t.length
                ? (0, W.jsx)(b.BM, {
                    className: Xe,
                    scrollY: !0,
                    scrollWithAnimation: !0,
                    showScrollbar: !1,
                    enhanced: !0,
                    fastDeceleration: !0,
                    children: (0, W.jsx)(b.Ss, {
                      className: Ue,
                      children:
                        null == s
                          ? void 0
                          : s.map(function (e) {
                              return (0, W.jsx)(
                                V.Fragment,
                                {
                                  children: (0, W.jsx)(Re, {
                                    item: e,
                                    titleDay: a,
                                    onSelect: l,
                                    selectList: t,
                                  }),
                                },
                                null == e ? void 0 : e.reserveId
                              );
                            }),
                    }),
                  })
                : null,
            ],
          });
        },
        Qe = n(8790),
        qe = "index-module__emptyConent___K14uG",
        Je = "index-module__empty___OWJJh";
      var Ke = function EmptyLoading() {
          return (0, W.jsx)(b.Ss, {
            className: qe,
            children: (0, W.jsx)(b.Ss, {
              className: Je,
              children: (0, W.jsx)(Qe.A, {
                size: "24px",
                vertical: !0,
                children: "加载中...",
              }),
            }),
          });
        },
        et = "index-module__emptyConent___hIRPI",
        tt = "index-module__empty___wxHrq",
        nt = "index-module__emptyImg___PY97w",
        it = "index-module__emptyText___HXNV_";
      var rt = function OrderEmpty_EmptyContent() {
          return (0, W.jsx)(b.Ss, {
            className: et,
            children: (0, W.jsxs)(b.Ss, {
              className: tt,
              children: [
                (0, W.jsx)(b._V, {
                  className: nt,
                  webp: !0,
                  mode: "aspectFill",
                  src: te.UG,
                }),
                (0, W.jsx)(b.EY, {
                  className: it,
                  children: "今日无可预订场地",
                }),
                (0, W.jsx)(b.EY, {
                  className: it,
                  children: "看看其他时间吧",
                }),
              ],
            }),
          });
        },
        at = n(7545),
        ot = "index-module__gymSitePicLogo___C6579",
        st = "index-module__gymSiteLogo___nvcjK",
        lt = "index-module__gymSiteLogoText___xtcQ3",
        ct = n(1850),
        dt = n(6618),
        ut = function previewPictureOrVideo(e) {
          dt.default.isLedongli
            ? (0, ct.Xe)(e, -1, "image")
            : (0, ct.Xe)((0, ct.PM)(e, []), 0);
        };
      var pt = function GymIntroductionLogo(e) {
          var t = e.info || {},
            n = t.courtList,
            i = t.skuDiscountInfoList,
            r = (0, V.useMemo)(
              function () {
                return (
                  (null == n
                    ? void 0
                    : n.reduce(function (e, t) {
                        return [].concat(
                          (0, a.A)(e),
                          (0, a.A)(t.avatars || [])
                        );
                      }, [])) || []
                );
              },
              [n]
            );
          return (i && (null == i ? void 0 : i.length) >= 2) || r.length < 1
            ? null
            : (0, W.jsxs)(b.Ss, {
                className: ot,
                onClick: function onClick() {
                  ut(r);
                },
                children: [
                  (0, W.jsx)(b._V, {
                    className: st,
                    webp: !0,
                    mode: "aspectFill",
                    src: k,
                  }),
                  (0, W.jsx)(b.EY, {
                    className: lt,
                    children: "分布",
                  }),
                ],
              });
        },
        ht = n(6864),
        mt = "index-module__gymIntroduction___dL3ij",
        ft = "index-module__gymSitePic___PSVOI",
        vt = "index-module__gymSiteLogo___irTx7",
        gt = "index-module__gymSiteText___eV0BV",
        wt = "index-module__gymSiteTypeContainer___UnHv9",
        bt = "index-module__gymSiteType___PbG2y",
        yt = "index-module__gymSiteTypeCentent___VLALn",
        At = "index-module__gymSiteTypeWrap___HJdwo",
        xt = "index-module__gymSiteTypeIcon___a8cY9",
        _t = "index-module__gymSiteTypeText___rKPwm",
        Et = "index-module__discountcontent___RuKPO",
        St = "index-module__mothlyFlag___bS4LY",
        Ct = n(9739);
      var Tt = function GymIntroduction(e) {
          var t = e.info || {},
            n = t.courtList,
            i = t.skuDiscountInfoList,
            r = (0, V.useMemo)(
              function () {
                return (
                  (null == n
                    ? void 0
                    : n.reduce(function (e, t) {
                        return [].concat(
                          (0, a.A)(e),
                          (0, a.A)(t.avatars || [])
                        );
                      }, [])) || []
                );
              },
              [n]
            );
          return !i || (null == i ? void 0 : i.length) < 2
            ? null
            : (0, W.jsxs)(b.Ss, {
                className: mt,
                children: [
                  (null == r ? void 0 : r.length) > 0
                    ? (0, W.jsxs)(b.Ss, {
                        className: ft,
                        onClick: function onClick() {
                          ut(r);
                        },
                        children: [
                          (0, W.jsx)(b._V, {
                            className: vt,
                            webp: !0,
                            mode: "aspectFill",
                            src: k,
                          }),
                          (0, W.jsx)(b.EY, {
                            className: gt,
                            children: "场地分布",
                          }),
                        ],
                      })
                    : null,
                  i && i.length > 0
                    ? (0, W.jsx)(b.Ss, {
                        className: wt,
                        children: (0, W.jsx)(b.BM, {
                          scrollX: !0,
                          enableFlex: !0,
                          showScrollbar: !1,
                          enhanced: !0,
                          className: bt,
                          scrollWithAnimation: !0,
                          fastDeceleration: !0,
                          children: (0, W.jsx)(b.Ss, {
                            className: yt,
                            style: {
                              justifyContent: "flex-start",
                            },
                            children:
                              null == i
                                ? void 0
                                : i.map(function (e, t) {
                                    var n,
                                      i,
                                      r,
                                      a = {
                                        discountType:
                                          "PERCENT" === e.calculateType
                                            ? Ne.dE.DISCOUNT_RATE
                                            : Ne.dE.DECREASE_MONEY,
                                        discountValue: e.discountValue,
                                        marketingDetailRule: {
                                          buyLimitRule: {
                                            isCheckBuyLimit:
                                              (0, G.IQ)(
                                                e.discountLimitNumber,
                                                0
                                              ) > 0,
                                            limit: e.discountLimitNumber,
                                          },
                                        },
                                        activityId: -1,
                                        companyId: -1,
                                        stockType: Ne.Mr.DAILY,
                                      },
                                      o = (0, ht.Xr)({
                                        productType: Ne.ch.BOOKING,
                                        platformMarketingDetail: a,
                                      });
                                    return (0, W.jsxs)(
                                      b.Ss,
                                      {
                                        className: At,
                                        children: [
                                          (0, W.jsx)(b.Ss, {
                                            className: xt,
                                            style: {
                                              background: "".concat(
                                                (null === (n = E[t]) ||
                                                void 0 === n
                                                  ? void 0
                                                  : n.color) || "#D4D4D4"
                                              ),
                                            },
                                          }),
                                          (0, W.jsx)(b.Ss, {
                                            className: _t,
                                            children: e.commoditySkuName,
                                          }),
                                          (0, W.jsx)(b.Ss, {
                                            className: Et,
                                            children: (0, W.jsx)(Be, {
                                              discountText:
                                                null == o ||
                                                null === (i = o[0]) ||
                                                void 0 === i
                                                  ? void 0
                                                  : i.data,
                                              couponType: e.couponType,
                                            }),
                                          }),
                                          "月挑" ===
                                            (null == e ||
                                            null === (r = e.tags) ||
                                            void 0 === r
                                              ? void 0
                                              : r[0]) &&
                                            (0, W.jsx)(b.Ss, {
                                              className: St,
                                              children: (0, W.jsx)(Ct.A, {}),
                                            }),
                                        ],
                                      },
                                      e.commoditySkuId
                                    );
                                  }),
                          }),
                        }),
                      })
                    : null,
                ],
              });
        },
        kt = n(3706),
        Mt = "index-module__noticeContent___RK9S_",
        It = "index-module__leftIconBox___jLkVJ",
        Lt = "index-module__leftIcon___YxQwX";
      var Pt = function OrderNotice(e) {
          var t = e.sellTimeRemind,
            n = (0, V.useMemo)(
              function () {
                return (0, W.jsx)(
                  kt.A,
                  {
                    color: "rgba(255,96,34,.8)",
                    background: "#FEEFE8",
                    text: t,
                    className: Mt,
                    renderLeftIcon: (0, W.jsx)(b.Ss, {
                      className: It,
                      children: (0, W.jsx)(b._V, {
                        className: Lt,
                        webp: !0,
                        mode: "aspectFill",
                        src: "https://img.alicdn.com/imgextra/i2/O1CN014JXJiN1juVvUzj1KU_!!6000000004608-49-tps-64-64.webp",
                        lazyLoad: !0,
                      }),
                    }),
                  },
                  t || Math.random()
                );
              },
              [t]
            );
          return t ? n : (0, W.jsx)(b.Ss, {});
        },
        Nt = n(5428),
        Ot = "index-module__buyNoticeScroll___HaVkU",
        Dt = "index-module__confirmBtn___Tkz40",
        zt = "index-module__remarkContent___zamwS",
        jt = "index-module__refundNotice___KoZf0",
        Bt = "index-module__tableContent___fALb4",
        Rt = "index-module__tableCellContent___fZw9V",
        Ft = "index-module__tableHeadCell___wk8dV",
        $t = "index-module__tableCell___Nr1Ml",
        Yt = "index-module__tableContentCell___XDPvD",
        Gt = "index-module__colBranch___uVyNS",
        Vt = "index-module__extra___gtCPu",
        Wt = "index-module__refundTitle___FmbZ1",
        Ht = "index-module__refundWrap___yoS66",
        Xt = n(1448);
      var Ut = function BuyActionSheet(e) {
          var t = (0, V.useState)(!1),
            n = (0, d.A)(t, 2),
            i = n[0],
            r = n[1],
            a = e.onClose,
            o = e.refundContent,
            s = void 0 === o ? [] : o,
            c = e.remarks,
            u = void 0 === c ? [] : c,
            p = e.onConfirm,
            h = e.isAll,
            m = void 0 !== h && h,
            f = e.btnText,
            g = void 0 === f ? "我知道了" : f,
            w = (0, Xt.d4)(function (e) {
              return {
                visible: e.OrderSiteModel.refundVisible,
              };
            }).visible,
            y = (0, V.useMemo)(
              function () {
                var e = "";
                return (
                  m &&
                    u &&
                    Array.isArray(u) &&
                    (null == u ? void 0 : u.length) > 0 &&
                    (e = "购买说明"),
                  s &&
                    Array.isArray(s) &&
                    (null == s ? void 0 : s.length) > 0 &&
                    (e = "退款说明"),
                  m &&
                    u &&
                    Array.isArray(u) &&
                    (null == u ? void 0 : u.length) > 0 &&
                    s &&
                    Array.isArray(s) &&
                    (null == s ? void 0 : s.length) > 0 &&
                    (e = "购买说明&退款说明"),
                  e
                );
              },
              [u, s, m]
            ),
            A = (0, V.useMemo)(
              function () {
                var e = [];
                return (
                  w &&
                    Array.isArray(w) &&
                    (null == w ? void 0 : w.length) > 0 &&
                    (e = w),
                  !Array.isArray(w) &&
                    s &&
                    Array.isArray(s) &&
                    s.length &&
                    (e = s),
                  e
                );
              },
              [s, w]
            ),
            x = function getRefundContent(e) {
              if ("object" === (0, l.A)(e)) {
                var t = [],
                  n = e.used,
                  i = e.expiredUnused,
                  r = e.unused;
                return (
                  "string" == typeof n &&
                    t.push({
                      title: "已使用",
                      content: n,
                    }),
                  "string" == typeof i &&
                    t.push({
                      title: "已过期(未使用)",
                      content: i,
                    }),
                  "string" == typeof r &&
                    t.push({
                      title: "未使用",
                      content: r,
                    }),
                  t
                );
              }
              return [];
            };
          return (0, W.jsxs)(Nt.A, {
            show: w,
            title: y,
            onClose: a,
            safeAreaInsetBottom: !0,
            children: [
              (0, W.jsxs)(b.BM, {
                className: Ot,
                scrollY: !0,
                scrollWithAnimation: !0,
                showScrollbar: !1,
                enhanced: !0,
                fastDeceleration: !0,
                children: [
                  (null == u ? void 0 : u.length) > 0 && m
                    ? (0, W.jsx)(b.Ss, {
                        children:
                          null == u
                            ? void 0
                            : u.map(function (e, t) {
                                return (0, W.jsxs)(W.Fragment, {
                                  children: [
                                    (0, W.jsx)(b.Ss, {
                                      className: Wt,
                                      children: e.name,
                                    }),
                                    (0, W.jsx)(b.Ss, {
                                      className: jt,
                                      children: "购买须知",
                                    }),
                                    (0, W.jsx)(b.Ss, {
                                      className: zt,
                                      children: e.remark,
                                    }),
                                  ],
                                });
                              }),
                      })
                    : null,
                  null == A
                    ? void 0
                    : A.map(function (e) {
                        var t;
                        return (0, W.jsxs)(b.Ss, {
                          className: Ht,
                          children: [
                            (0, W.jsx)(b.Ss, {
                              className: Wt,
                              children: e.name,
                            }),
                            (0, W.jsx)(b.Ss, {
                              className: jt,
                              children: "退款说明",
                            }),
                            (0, W.jsxs)(b.Ss, {
                              className: Bt,
                              children: [
                                (0, W.jsxs)(b.Ss, {
                                  className: Rt,
                                  children: [
                                    (0, W.jsx)(b.Ss, {
                                      className: v()(Ft, $t),
                                      children: "商品状态",
                                    }),
                                    (0, W.jsx)(b.Ss, {
                                      className: v()(Ft, $t),
                                      children: "退款金额或比例",
                                    }),
                                  ],
                                }),
                                null != e &&
                                e.refund &&
                                (null === (t = Object.keys(e.refund)) ||
                                void 0 === t
                                  ? void 0
                                  : t.length) > 0
                                  ? x(e.refund).map(function (e) {
                                      var t;
                                      return (0, W.jsxs)(b.Ss, {
                                        className: Rt,
                                        children: [
                                          (0, W.jsx)(b.Ss, {
                                            className: v()(Yt, $t),
                                            children: e.title,
                                          }),
                                          (0, W.jsx)(b.Ss, {
                                            className: Gt,
                                            children:
                                              null === (t = e.content) ||
                                              void 0 === t ||
                                              null === (t = t.split(";")) ||
                                              void 0 === t
                                                ? void 0
                                                : t.map(function (e) {
                                                    return e
                                                      ? (0, W.jsx)(b.Ss, {
                                                          className: v()(
                                                            Yt,
                                                            $t
                                                          ),
                                                          children: e,
                                                        })
                                                      : null;
                                                  }),
                                          }),
                                        ],
                                      });
                                    })
                                  : null,
                                (0, W.jsx)(b.Ss, {
                                  className: Vt,
                                  children:
                                    "注：下单多个商品或预定多片场地时实际退款金额请以退款时计算金额为准，如有疑问请电话商家确认。",
                                }),
                              ],
                            }),
                          ],
                        });
                      }),
                ],
              }),
              (0, W.jsx)(b.$n, {
                className: Dt,
                onClick: function handleClose() {
                  r(!0),
                    !m && p && p(),
                    a(),
                    setTimeout(function () {
                      r(!1);
                    }, 1e3);
                },
                disabled: !!i || void 0,
                children: g,
              }),
            ],
          });
        },
        Zt = "index-module__BuyNotice___eVkT5",
        Qt = "index-module__textDecorate___G8T5L",
        qt = "index-module__buyNoticeArrow___K2fFg";
      var Jt = function BuyNotice(e) {
          var t = e.outStyle,
            n = void 0 === t ? {} : t,
            i = e.data,
            r = e.onConfirm,
            a = (0, Xt.wA)(),
            o = (0, V.useState)(!1),
            s = (0, d.A)(o, 2),
            l = s[0],
            c = s[1],
            u = (0, V.useMemo)(
              function () {
                var e;
                return i && Array.isArray(i)
                  ? null == i ||
                    null ===
                      (e = i.filter(function (e) {
                        return !!e.remark;
                      })) ||
                    void 0 === e ||
                    null === (e = e[0]) ||
                    void 0 === e
                    ? void 0
                    : e.remark
                  : "";
              },
              [i]
            ),
            p = (0, V.useMemo)(
              function () {
                return i && Array.isArray(i) && l
                  ? null == i
                    ? void 0
                    : i
                        .filter(function (e) {
                          return !!e.remark;
                        })
                        .map(function (e) {
                          return {
                            name: e.name,
                            remark: e.remark,
                          };
                        })
                  : [];
              },
              [i, l]
            ),
            h = (0, V.useMemo)(
              function () {
                return i && Array.isArray(i)
                  ? null == i
                    ? void 0
                    : i.filter(function (e) {
                        var t;
                        return (
                          (null == e ? void 0 : e.refund) &&
                          (null === (t = Object.keys(e.refund)) || void 0 === t
                            ? void 0
                            : t.length) > 0
                        );
                      })
                  : [];
              },
              [i]
            );
          return (0, W.jsxs)(b.RW, {
            children: [
              u
                ? (0, W.jsxs)(b.Ss, {
                    className: Zt,
                    style: n,
                    onClick: function handleClick(e) {
                      null == e || e.preventDefault(),
                        c(!0),
                        a({
                          type: "OrderSiteModel/saveFundVisible",
                          payload: !0,
                        });
                    },
                    children: [
                      (0, W.jsxs)(b.EY, {
                        className: Qt,
                        children: ["购买须知：", u],
                      }),
                      (0, W.jsx)(b._V, {
                        className: qt,
                        webp: !0,
                        mode: "aspectFill",
                        src: "https://gw.alicdn.com/imgextra/i3/O1CN01H7uXex1ekpVRWGR3g_!!6000000003910-49-tps-48-48.webp",
                      }),
                    ],
                  })
                : null,
              (0, W.jsx)(Ut, {
                remarks: p,
                onConfirm: r,
                refundContent: h,
                isAll: l,
                onClose: function handleClose() {
                  a({
                    type: "OrderSiteModel/saveFundVisible",
                    payload: !1,
                  }),
                    c(!1);
                },
                btnText: "我知道了",
              }),
            ],
          });
        },
        Kt = n(9839),
        en = "index-module__content____y6_5",
        tn = function ConfirmModal(e) {
          var t = e.show,
            n = e.confirmCallback,
            i = e.cancelCalllback;
          return (0, W.jsx)(Kt.A, {
            show: t,
            title: "场次已开始提醒",
            confirmText: "确定",
            descText: "我再想想",
            descStyle: {
              color: "#999",
            },
            onDescClick: i,
            onConfirm: n,
            onClose: i,
            children: (0, W.jsx)(g.A, {
              className: en,
              children: "您选择的场次包含已经开始的场次，请确认时间安排。",
            }),
          });
        },
        nn = n(2562),
        rn = (function (e) {
          return (
            (e.ORDER_VIEW = "order_view"),
            (e.SIT_EMPTY_SUBSCRIPTION_VIEW = "sit_empty_subscription_view"),
            e
          );
        })({}),
        an = (function (e) {
          return (
            (e.ORDER_BTN = "order_btn"),
            (e.SIT_EMPTY_SUBSCRIPTION_BTN = "sit_empty_subscription_btn"),
            e
          );
        })({}),
        sn = (0, i.A)(
          (0, i.A)(
            {},
            an.ORDER_BTN,
            "".concat(rn.ORDER_VIEW, ".").concat(an.ORDER_BTN)
          ),
          an.SIT_EMPTY_SUBSCRIPTION_BTN,
          ""
            .concat(rn.SIT_EMPTY_SUBSCRIPTION_VIEW, ".")
            .concat(an.SIT_EMPTY_SUBSCRIPTION_BTN)
        ),
        ln = n(817),
        cn = "index-module__pageContent___j1swA",
        dn = "index-module__gymListWrapMaskContent___JZYkC",
        un = "index-module__gymListWrapMask___ZZcuL",
        pn = "index-module__gymListWrap___pDSR3",
        ssu_just_do_it = "ssu_just_do_it",
        hn = "index-module__gymListScrollView___UTj9O",
        mn = "index-module__gymItem___fmVBV",
        fn = "index-module__gymItemname___RGjDI",
        vn = "index-module__pageContentScroolView___tblCH",
        gn = "index-module__flexWrap___wdS5C",
        wn = "index-module__pageContentScroolViewTime___mmzn_",
        bn = "index-module__timeItem___piX2i",
        yn = "index-module__timeName___L8PUu",
        An = "index-module__dot___rmeI_",
        xn = "index-module__dashline___xMzdO",
        _n = "index-module__pageContentScroolViewGymList___w7Lxd",
        En = "index-module__gymItemX___VLsi4",
        Sn = "index-module__bottomWrapper___QLzGl",
        Cn = "index-module__orderFeeWrap___wdujZ",
        Tn = "index-module__ordeFee___BnRSJ",
        kn = "index-module__realFee___KAawj",
        Mn = "index-module__calFeeText___xmfc5",
        In = "index-module__discountFee___zmjoQ",
        Ln = "index-module__orderBtn___nFmnn",
        Pn = "index-module__orderBtnText___QM_qn",
        Nn = "index-module__canOrder___guRn4",
        On = "index-module__discountImg___xbHTy",
        Dn = "index-module__discountContainer___LngXk",
        zn = "index-module__dicountContent___O08Ab",
        jn = "index-module__discountArrow___u5cq7",
        Bn = n(3146),
        Rn = function useSiteSubscription() {
          var e = (0, V.useState)(!1),
            t = (0, d.A)(e, 2),
            n = t[0],
            i = t[1],
            r = (0, V.useState)(""),
            a = (0, d.A)(r, 2),
            l = a[0],
            c = a[1],
            u = (0, V.useState)([]),
            h = (0, d.A)(u, 2),
            f = h[0],
            v = h[1],
            g = (0, m.A)().params,
            w = (void 0 === g ? {} : g) || {},
            b = w.propertyValueId,
            y = w.stadiumId;
          (0, V.useEffect)(function () {
            A();
          }, []);
          var A = (function () {
              var e = (0, s.A)(
                (0, o.A)().mark(function _callee() {
                  var e, t, n;
                  return (0, o.A)().wrap(function _callee$(r) {
                    for (;;)
                      switch ((r.prev = r.next)) {
                        case 0:
                          return r.abrupt("return");
                        case 2:
                          return (
                            (r.next = 4),
                            (0, Bn.t)({
                              isCheckByAssociationType: !0,
                              associationType: Bn.W9.SIT_EMPTY_TIP,
                            })
                          );
                        case 4:
                          (e = r.sent),
                            (t = e.checkType),
                            (n = e.currentTempleteIds),
                            v(n || []),
                            c(t),
                            console.log("订场检查模板状态", t),
                            Bn.Nq.REJECT !== t ? i(!0) : i(!1);
                        case 12:
                        case "end":
                          return r.stop();
                      }
                  }, _callee);
                })
              );
              return function queryTempleteInfo() {
                return e.apply(this, arguments);
              };
            })(),
            x = (function () {
              var e = (0, s.A)(
                (0, o.A)().mark(function _callee2(e) {
                  var t, n, r, a, s, d, u, h;
                  return (0, o.A)().wrap(
                    function _callee2$(o) {
                      for (;;)
                        switch ((o.prev = o.next)) {
                          case 0:
                            return (
                              (t = "".concat(y, "#").concat(b, "#").concat(e)),
                              (o.next = 3),
                              (0, Bn.t)({
                                isCheckByAssociationType: !0,
                                associationType: Bn.W9.SIT_EMPTY_TIP,
                              })
                            );
                          case 3:
                            if (
                              ((n = o.sent),
                              (r = n.checkType),
                              (a = n.lastTempListDataList),
                              r !== Bn.Nq.REJECT)
                            ) {
                              o.next = 9;
                              break;
                            }
                            return i(!1), o.abrupt("return");
                          case 9:
                            if (Bn.Nq.ACCEPT !== l) {
                              o.next = 21;
                              break;
                            }
                            return (
                              (o.next = 12),
                              (0, Bn.Do)({
                                isCheckByAssociationType: !1,
                                associationType: Bn.W9.SIT_EMPTY_TIP,
                                checkTemplateList: a,
                              })
                            );
                          case 12:
                            if (!(s = o.sent).isSuccess) {
                              o.next = 19;
                              break;
                            }
                            if (s.isOpenSetting) {
                              o.next = 19;
                              break;
                            }
                            return (
                              (0, p.P0)({
                                title: "已成功订阅一次",
                                icon: "none",
                                duration: 1e3,
                              }),
                              (o.next = 18),
                              (0, Bn.DU)({
                                isNeedFetchTemplete: !1,
                                templateIds: f,
                                associationType: Bn.W9.SIT_EMPTY_TIP,
                                checkNoticeSomeSwitch: !0,
                                associationId: t,
                              })
                            );
                          case 18:
                            o.sent;
                          case 19:
                            o.next = 37;
                            break;
                          case 21:
                            return (
                              (o.prev = 21),
                              (o.next = 24),
                              (0, Bn.DU)({
                                isNeedFetchTemplete: !1,
                                templateIds: f,
                                associationType: Bn.W9.SIT_EMPTY_TIP,
                                checkNoticeSomeSwitch: !0,
                                associationId: t,
                                isAgreeShowToast: !0,
                                agreeShowToastMessage: "已成功订阅一次",
                              })
                            );
                          case 24:
                            if (null == (d = o.sent) || !d.isSuccess) {
                              o.next = 32;
                              break;
                            }
                            return (
                              d.isAllReject && (c(Bn.Nq.REJECT), i(!1)),
                              (o.next = 29),
                              (0, Bn.t)({
                                isCheckByAssociationType: !0,
                                associationType: Bn.W9.SIT_EMPTY_TIP,
                              })
                            );
                          case 29:
                            (u = o.sent), (h = u.checkType), c(h);
                          case 32:
                            o.next = 37;
                            break;
                          case 34:
                            (o.prev = 34),
                              (o.t0 = o.catch(21)),
                              console.log("手动订阅结果-error", o.t0);
                          case 37:
                          case "end":
                            return o.stop();
                        }
                    },
                    _callee2,
                    null,
                    [[21, 34]]
                  );
                })
              );
              return function handleSiteSubscriptions(t) {
                return e.apply(this, arguments);
              };
            })();
          return {
            queryTempleteInfo: A,
            isShowSubscriptTipPop: n,
            setIsShowSubscriptTipPop: i,
            handleSiteSubscriptions: x,
          };
        },
        Fn = (n(5019), n(7160), !0),
        $n = null,
        Yn = (0, u.A)({
          spmA: nn.Ar.ALI_SPORTS,
          spmB: nn.tu.ORDER_SITE,
          header: !0,
          title: "选择场地",
          bottomArea: !1,
          arrowLeft:
            "https://gw.alicdn.com/imgextra/i1/O1CN01bWsGSq1fpEt7wTsf5_!!6000000004055-49-tps-36-68.webp",
          titleColor: "#fff",
          outStyle: {
            backgroundColor: "#222",
          },
        })(function OrderSite() {
          var e,
            t,
            n,
            u = (0, V.useState)(!1),
            f = (0, d.A)(u, 2),
            g = f[0],
            w = f[1],
            y = (0, V.useState)(!1),
            x = (0, d.A)(y, 2),
            _ = x[0],
            E = x[1],
            S = (0, V.useState)(!1),
            T = (0, d.A)(S, 2),
            k = T[0],
            M = T[1],
            P = (0, V.useState)({
              discountedPrice: "0",
              originPrice: "0",
            }),
            N = (0, d.A)(P, 2),
            R = N[0],
            H = N[1],
            X = (0, V.useState)(null),
            U = (0, d.A)(X, 2),
            Z = U[0],
            Q = U[1],
            q = (0, V.useRef)(null),
            J = (0, V.useState)([]),
            te = (0, d.A)(J, 2),
            ne = te[0],
            ie = te[1],
            re = Rn(),
            oe = re.isShowSubscriptTipPop,
            ce = re.queryTempleteInfo,
            de =
              (re.handleSiteSubscriptions,
              (0, Xt.d4)(function (e) {
                var t = e.OrderSiteModel,
                  n = e.loading;
                return {
                  List: t.List,
                  Detail: t.detail,
                  orderSiteInfo: t.orderSiteInfo,
                  fetchListLoading: n.effects["OrderSiteModel/fetchList"],
                  fetchDetailLoading: n.effects["OrderSiteModel/fetchDetail"],
                  fetchDailyInfoLoading:
                    n.effects["OrderSiteModel/queryDailyInfo"],
                };
              })),
            ue = de.List,
            pe = de.Detail,
            he = de.orderSiteInfo,
            me = de.fetchListLoading,
            fe = void 0 === me || me,
            ve = de.fetchDetailLoading,
            ge = void 0 === ve || ve,
            we = de.fetchDailyInfoLoading,
            be = void 0 === we || we,
            ye = (0, V.useState)({}),
            Ae = (0, d.A)(ye, 2),
            Ee = Ae[0],
            Se = Ae[1],
            Ce = (0, V.useState)(""),
            Te = (0, d.A)(Ce, 2),
            ke = Te[0],
            Me = Te[1],
            Ie = (0, m.A)().params,
            Le = void 0 === Ie ? {} : Ie,
            Pe = (0, Xt.wA)(),
            Ne = (0, V.useState)(""),
            Oe = (0, d.A)(Ne, 2),
            De = Oe[0],
            ze = Oe[1],
            je = le(),
            Be = (0, V.useState)(je),
            Re = (0, d.A)(Be, 2),
            Fe = Re[0],
            $e = Re[1],
            Ye = (0, V.useRef)([]);
          (0, ee.A)(
            function () {
              if (Le.date) {
                var e = C()(Number(Le.date)).format("DD"),
                  t = C()(Number(Le.date)).format("MM"),
                  n = C()(Number(Le.date)).format("YYYY");
                qe(e, t, n),
                  setTimeout(function () {
                    ze("ID-".concat(e, "-").concat(t, "-").concat(n));
                  }, 2e3);
              }
            },
            [null == Le ? void 0 : Le.date]
          );
          var Ge = (0, V.useMemo)(
              function () {
                var e = g;
                return (
                  (null == ne ? void 0 : ne.length) <= 3 && (w(!1), (e = !1)),
                  e ? ne : null == ne ? void 0 : ne.slice(0, 3)
                );
              },
              [g, ne]
            ),
            Ve = (0, V.useCallback)(
              function (e) {
                Pe({
                  type: "OrderSiteModel/fetchList",
                  payload: {
                    date: e,
                    pageNo: 0,
                    pageSize: 200,
                    stadiumId: null == Le ? void 0 : Le.stadiumId,
                    propertyValueId: null == Le ? void 0 : Le.propertyValueId,
                  },
                });
              },
              [
                Pe,
                null == Le ? void 0 : Le.propertyValueId,
                null == Le ? void 0 : Le.stadiumId,
              ]
            ),
            We = (0, V.useCallback)(
              function (e) {
                Pe({
                  type: "OrderSiteModel/fetchDetail",
                  payload: {
                    date: e,
                    propertyValueId: null == Le ? void 0 : Le.propertyValueId,
                    stadiumId: null == Le ? void 0 : Le.stadiumId,
                  },
                });
              },
              [
                Pe,
                null == Le ? void 0 : Le.propertyValueId,
                null == Le ? void 0 : Le.stadiumId,
              ]
            );
          (0, ee.A)(
            function () {
              if (Z) {
                console.error("2222222222");
                var e = Z || {},
                  t = e.num,
                  n = e.month,
                  i = e.year;
                Ve("".concat(i, "-").concat(n, "-").concat(t)),
                  We("".concat(i, "-").concat(n, "-").concat(t));
              }
            },
            [Z, Le, Pe, Ve, We]
          ),
            (0, ee.A)(
              function () {
                var e = C()().format("DD"),
                  t = Z ? (null == Z ? void 0 : Z.num) : je.today;
                if (he && ue && pe) {
                  var n = (function getCurrentMin() {
                      var e = C()(),
                        t = e.hour();
                      return e.set("hour", t).set("minute", 0).set("second", 0);
                    })().format("HHmm"),
                    i = setTimeout(function () {
                      if (document) {
                        var i = document.getElementById("ele".concat(n));
                        i && e === t && i.scrollIntoView();
                      }
                      Fn = !1;
                    }, 500);
                  return function () {
                    i && clearTimeout(i);
                  };
                }
              },
              [he, ue, pe, ke, Z]
            ),
            (0, _e.A)(function (e) {
              if (!e && !Fn)
                if ((console.error(Fn, "=========="), Z)) {
                  var t = Z || {},
                    n = t.num,
                    i = t.month,
                    r = t.year;
                  Ve("".concat(r, "-").concat(i, "-").concat(n)),
                    We("".concat(r, "-").concat(i, "-").concat(n));
                } else Ve(je.current), We(je.current);
            });
          var He = (0, V.useMemo)(
            function () {
              var e, t;
              if (
                1 !== (null === (e = I(ne)) || void 0 === e ? void 0 : e.length)
              )
                return null;
              var n =
                  (
                    (null ===
                      (t = ne.filter(function (e) {
                        return !!e.discountInfo;
                      })) || void 0 === t
                      ? void 0
                      : t[0]) ||
                    {} ||
                    {}
                  ).discountInfo || {},
                i = n.couponType,
                r = n.discountValue,
                a = n.calculateType;
              if (i === A.SUPERMEMBER)
                return (0, W.jsx)(b._V, {
                  className: On,
                  webp: !0,
                  mode: "aspectFill",
                  src: "https://gw.alicdn.com/imgextra/i3/O1CN01fMQUyH1IlMNDWaZVp_!!6000000000933-49-tps-212-74.webp",
                });
              if (i === A.LIMITEDTIME) {
                var o,
                  s = parseLimitActivityText({
                    platformMarketingDetail: {
                      discountType: a,
                      discountValue: r,
                    },
                  });
                return (0, W.jsxs)(b.Ss, {
                  className: Dn,
                  children: [
                    (0, W.jsx)(b.Ss, {
                      className: zn,
                      children:
                        null == s || null === (o = s[0]) || void 0 === o
                          ? void 0
                          : o.data,
                    }),
                    (0, W.jsx)(b._V, {
                      className: jn,
                      webp: !0,
                      mode: "aspectFill",
                      src: "https://gw.alicdn.com/imgextra/i2/O1CN01N219WF1Heey7qX9AA_!!6000000000783-49-tps-16-16.webp",
                    }),
                  ],
                });
              }
              return null;
            },
            [ne]
          );
          (0, V.useEffect)(
            function () {
              return (
                null != Le &&
                  Le.propertyValueId &&
                  null != Le &&
                  Le.stadiumId &&
                  je.current &&
                  (console.error("333333333"),
                  (null == Le || !Le.date) && Ve(je.current),
                  (null == Le || !Le.date) && We(je.current),
                  Ue(je.weekList)),
                function () {
                  Pe({
                    type: "OrderSiteModel/initDate",
                  }),
                    (Fn = !0);
                }
              );
            },
            [je]
          ),
            (0, ee.A)(
              function () {
                if (null != he && he.length) {
                  var e = !0,
                    t = Fe.weekList.map(function (t, n) {
                      var i = he[n];
                      return (
                        null != t && t.setWeekList && (e = !1),
                        (0, c.A)(
                          (0, c.A)((0, c.A)({}, t), i),
                          {},
                          {
                            setWeekList: !0,
                          }
                        )
                      );
                    });
                  e &&
                    $e(
                      (0, c.A)(
                        (0, c.A)({}, Fe),
                        {},
                        {
                          weekList: t,
                        }
                      )
                    );
                  var n = Z
                    ? ""
                        .concat(null == Z ? void 0 : Z.year, "-")
                        .concat(null == Z ? void 0 : Z.month, "-")
                        .concat(null == Z ? void 0 : Z.num)
                    : Fe.current;
                  Je(n);
                }
              },
              [he, Z, Fe]
            );
          var Xe = (0, V.useMemo)(
              function () {
                if (Ee && "object" === (0, l.A)(Ee) && Object.keys(Ee).length) {
                  var e = (Ee || {}).skuList,
                    t = void 0 === e ? [] : e,
                    n = {};
                  return (
                    (null == t
                      ? void 0
                      : t.map(function (e) {
                          var t = e || {},
                            i = t.commoditySkuId,
                            r = t.buyReading,
                            a = t.commoditySkuName,
                            o = t.transformRefundRule;
                          return (
                            (n = {
                              key: i,
                              name: a,
                              remark: r,
                            }),
                            o && "object" === (0, l.A)(o) && (n.refund = o),
                            n
                          );
                        })) || []
                  );
                }
                return [];
              },
              [Ee]
            ),
            Ue = function fetchDailyInfo(e) {
              var t,
                n,
                i,
                r,
                a,
                o,
                s = ""
                  .concat(
                    null === (t = e[0]) || void 0 === t ? void 0 : t.year,
                    "-"
                  )
                  .concat(
                    null === (n = e[0]) || void 0 === n ? void 0 : n.month,
                    "-"
                  )
                  .concat(null === (i = e[0]) || void 0 === i ? void 0 : i.num),
                l = ""
                  .concat(
                    null === (r = e[6]) || void 0 === r ? void 0 : r.year,
                    "-"
                  )
                  .concat(
                    null === (a = e[6]) || void 0 === a ? void 0 : a.month,
                    "-"
                  )
                  .concat(null === (o = e[6]) || void 0 === o ? void 0 : o.num);
              Pe({
                type: "OrderSiteModel/queryDailyInfo",
                payload: {
                  startDate: s,
                  endDate: l,
                  stadiumId: (null == Le ? void 0 : Le.stadiumId) || "",
                  propertyValueId:
                    (null == Le ? void 0 : Le.propertyValueId) || 32899175,
                },
              });
            },
            Qe = (0, V.useCallback)(function () {
              w(function (e) {
                return !e;
              });
            }, []),
            qe = function handleClickWeekItem(e, t, n) {
              H({
                discountedPrice: "0",
                originPrice: "0",
              }),
                Me(""),
                Q({
                  num: e,
                  month: t,
                  year: n,
                }),
                (Ye.current = []),
                ie([]),
                ce();
            },
            Je = function initCurrentOrderInfo(e) {
              var t = he.find(function (t) {
                return (
                  C()(t.date).format("YYYY-MM-DD") ===
                  C()(e).format("YYYY-MM-DD")
                );
              });
              t && Se(t);
            },
            et = (0, V.useCallback)(
              (function () {
                var e = (0, s.A)(
                  (0, o.A)().mark(function _callee(e) {
                    var t, n, i, r, a, s, l, c, d, u, h, m, f, v, g;
                    return (0, o.A)().wrap(function _callee$(o) {
                      for (;;)
                        switch ((o.prev = o.next)) {
                          case 0:
                            return (
                              E(function () {
                                return !0;
                              }),
                              (t = C()().valueOf()),
                              ($n = t),
                              (o.next = 5),
                              (0, ln.eJ)({
                                reserveItems: e,
                                stadiumId:
                                  (null == Le ? void 0 : Le.stadiumId) || "",
                              })
                            );
                          case 5:
                            if (((n = o.sent), $n !== t)) {
                              o.next = 19;
                              break;
                            }
                            if (
                              (E(function () {
                                return !1;
                              }),
                              null == n || !n.isSuccess)
                            ) {
                              o.next = 18;
                              break;
                            }
                            if (
                              ((i =
                                n.data || {
                                  canReserve: !1,
                                  message: "",
                                } ||
                                {}),
                              (r = i.canReserve),
                              (a = i.message),
                              (s = i.orderPrice),
                              !r)
                            ) {
                              o.next = 16;
                              break;
                            }
                            return (
                              (c = (l = s || {}).discountedPrice),
                              (d = void 0 === c ? 0 : c),
                              (u = l.originPrice),
                              (h = void 0 === u ? 0 : u),
                              (m = l.reserveItemPriceList),
                              (f = void 0 === m ? [] : m),
                              (v = (0, G.F)(d)),
                              (g = (0, G.F)(h)),
                              H({
                                discountedPrice: v,
                                originPrice: g,
                              }),
                              o.abrupt("return", f || [])
                            );
                          case 16:
                            return (
                              a &&
                                (0, p.P0)({
                                  title: a,
                                  icon: "none",
                                  duration: 2e3,
                                }),
                              o.abrupt("return", !1)
                            );
                          case 18:
                            return o.abrupt("return", !1);
                          case 19:
                            return o.abrupt("return", null);
                          case 20:
                          case "end":
                            return o.stop();
                        }
                    }, _callee);
                  })
                );
                return function (t) {
                  return e.apply(this, arguments);
                };
              })(),
              [null == Le ? void 0 : Le.stadiumId]
            ),
            tt = (0, V.useCallback)(
              (function () {
                var e = (0, s.A)(
                  (0, o.A)().mark(function _callee2(e) {
                    var t, n, i, r, s, l, d, u, p;
                    return (0, o.A)().wrap(function _callee2$(o) {
                      for (;;)
                        switch ((o.prev = o.next)) {
                          case 0:
                            if (
                              ((t = ne.findIndex(function (t) {
                                return t.reserveId === e.reserveId;
                              })),
                              (n = (0, a.A)(ne)),
                              !(t > -1))
                            ) {
                              o.next = 12;
                              break;
                            }
                            return (
                              (i =
                                null == ne
                                  ? void 0
                                  : ne.filter(function (e, n) {
                                      return n !== t;
                                    })),
                              ie(i),
                              (r =
                                null == i
                                  ? void 0
                                  : i.map(function (e) {
                                      var t = e || {};
                                      return {
                                        reserveId: t.reserveId,
                                        commoditySkuId: t.commodityId,
                                        pieceId: t.pieceId,
                                        reservePrice: t.originPrice,
                                      };
                                    })),
                              (o.next = 8),
                              et(r)
                            );
                          case 8:
                            (s = o.sent)
                              ? ((n =
                                  null == i
                                    ? void 0
                                    : i
                                        .map(function (e) {
                                          var t,
                                            n =
                                              null === (t = s || []) ||
                                              void 0 === t
                                                ? void 0
                                                : t.find(function (t) {
                                                    return (
                                                      t.reserveId ===
                                                      e.reserveId
                                                    );
                                                  });
                                          if (n) {
                                            var i = n || {},
                                              r = i.discountInfo,
                                              a = i.discountedPrice,
                                              o = i.originPrice;
                                            return (0, c.A)(
                                              (0, c.A)({}, e),
                                              {},
                                              {
                                                discountInfo: r,
                                                discountedPrice: a,
                                                originPrice: o,
                                              }
                                            );
                                          }
                                        })
                                        .filter(function (e) {
                                          return !!e;
                                        })),
                                (Ye.current = n),
                                ie(n))
                              : null !== s && ie(Ye.current),
                              (o.next = 19);
                            break;
                          case 12:
                            return (
                              n.push(e),
                              ie(n),
                              (d =
                                null === (l = (0, a.A)(n || [])) || void 0 === l
                                  ? void 0
                                  : l.map(function (e) {
                                      var t = e || {};
                                      return {
                                        reserveId: t.reserveId,
                                        commoditySkuId: t.commodityId,
                                        pieceId: t.pieceId,
                                        reservePrice: t.originPrice,
                                      };
                                    })),
                              (o.next = 17),
                              et(d)
                            );
                          case 17:
                            (u = o.sent)
                              ? ((n =
                                  null === (p = n) || void 0 === p
                                    ? void 0
                                    : p
                                        .map(function (e) {
                                          var t,
                                            n =
                                              null === (t = u || []) ||
                                              void 0 === t
                                                ? void 0
                                                : t.find(function (t) {
                                                    return (
                                                      t.reserveId ===
                                                      e.reserveId
                                                    );
                                                  });
                                          if (n) {
                                            var i = n || {},
                                              r = i.discountInfo,
                                              a = i.discountedPrice,
                                              o = i.originPrice;
                                            return (0, c.A)(
                                              (0, c.A)({}, e),
                                              {},
                                              {
                                                discountInfo: r,
                                                discountedPrice: a,
                                                originPrice: o,
                                              }
                                            );
                                          }
                                        })
                                        .filter(function (e) {
                                          return !!e;
                                        })),
                                (Ye.current = n),
                                ie(n))
                              : null !== u && ie(Ye.current);
                          case 19:
                          case "end":
                            return o.stop();
                        }
                    }, _callee2);
                  })
                );
                return function (t) {
                  return e.apply(this, arguments);
                };
              })(),
              [et, ne]
            ),
            nt = (0, V.useCallback)(
              (function () {
                var e = (0, s.A)(
                  (0, o.A)().mark(function _callee3(e) {
                    var t, n, i, r;
                    return (0, o.A)().wrap(function _callee3$(a) {
                      for (;;)
                        switch ((a.prev = a.next)) {
                          case 0:
                            return (
                              (t = ne.filter(function (t, n) {
                                return n !== e;
                              })),
                              ie(t),
                              (n =
                                null == t
                                  ? void 0
                                  : t.map(function (e) {
                                      var t = e || {};
                                      return {
                                        reserveId: t.reserveId,
                                        commoditySkuId: t.commodityId,
                                        pieceId: t.pieceId,
                                        reservePrice: t.originPrice,
                                      };
                                    })),
                              (a.next = 5),
                              et(n)
                            );
                          case 5:
                            (i = a.sent)
                              ? ((r =
                                  null == t
                                    ? void 0
                                    : t
                                        .map(function (e) {
                                          var t,
                                            n =
                                              null === (t = i || []) ||
                                              void 0 === t
                                                ? void 0
                                                : t.find(function (t) {
                                                    return (
                                                      t.reserveId ===
                                                      e.reserveId
                                                    );
                                                  });
                                          if (n) {
                                            var r = n || {},
                                              a = r.discountInfo,
                                              o = r.discountedPrice,
                                              s = r.originPrice;
                                            return (0, c.A)(
                                              (0, c.A)({}, e),
                                              {},
                                              {
                                                discountInfo: a,
                                                discountedPrice: o,
                                                originPrice: s,
                                              }
                                            );
                                          }
                                        })
                                        .filter(function (e) {
                                          return !!e;
                                        })),
                                (Ye.current = r),
                                ie(r))
                              : null !== i && ie(Ye.current);
                          case 7:
                          case "end":
                            return a.stop();
                        }
                    }, _callee3);
                  })
                );
                return function (t) {
                  return e.apply(this, arguments);
                };
              })(),
              [et, ne]
            ),
            it = (0, V.useMemo)(
              function () {
                if (Z) {
                  var e = Z || {},
                    t = e.num,
                    n = e.month;
                  return {
                    selectDay: "".concat(n, ".").concat(t),
                  };
                }
                return {
                  selectDay: "".concat(je.currentMonth, ".").concat(je.today),
                };
              },
              [je, Z]
            ),
            ot = function toOrderConfirm() {
              var e = (function uniqueCommodityId(e) {
                var t,
                  n = {},
                  i = (0, r.A)(e);
                try {
                  for (i.s(); !(t = i.n()).done; ) {
                    var a = t.value,
                      o = (a || {}).commodityId;
                    o && n[o] ? n[o].push(a) : (n[o] = [a]);
                  }
                } catch (e) {
                  i.e(e);
                } finally {
                  i.f();
                }
                return Object.keys(n).map(function (e) {
                  var t;
                  return {
                    id: e,
                    reserveNoList:
                      null === (t = n[e]) || void 0 === t
                        ? void 0
                        : t.map(function (e) {
                            return e.reserveId;
                          }),
                  };
                });
              })(ne);
              (0, ct.VJ)({
                url: "/pages/Orders/OrderConfirm/index?stadiumId="
                  .concat(null == Le ? void 0 : Le.stadiumId, "&commodityList=")
                  .concat(JSON.stringify(e)),
              });
            },
            st = (0, V.useCallback)(
              (0, s.A)(
                (0, o.A)().mark(function _callee4() {
                  var e, t, n, i, r, a, s, l;
                  return (0, o.A)().wrap(function _callee4$(o) {
                    for (;;)
                      switch ((o.prev = o.next)) {
                        case 0:
                          if (null != ne && ne.length) {
                            o.next = 2;
                            break;
                          }
                          return o.abrupt("return");
                        case 2:
                          return (
                            (e = ne.map(function (e) {
                              var t = e || {};
                              return {
                                reserveId: t.reserveId,
                                commoditySkuId: t.commodityId,
                                pieceId: t.pieceId,
                                reservePrice: t.originPrice,
                              };
                            })),
                            (0, ae.IC)("CLK", {
                              spm_cd: sn.order_btn,
                            }),
                            (o.next = 6),
                            (0, ln._M)({
                              reserveItems: e,
                              stadiumId:
                                (null == Le ? void 0 : Le.stadiumId) || "",
                            })
                          );
                        case 6:
                          if (!(t = o.sent).isSuccess) {
                            o.next = 18;
                            break;
                          }
                          if (
                            ((n = (null == t ? void 0 : t.data) || {}),
                            (i = n.needPopRefundRule),
                            (r = n.needPopRefundRuleSkuList),
                            !i)
                          ) {
                            o.next = 16;
                            break;
                          }
                          if (
                            ((a =
                              (null == r
                                ? void 0
                                : r.filter(function (e) {
                                    var t;
                                    return (
                                      (null == e
                                        ? void 0
                                        : e.transformRefundRule) &&
                                      (null ===
                                        (t = Object.keys(
                                          e.transformRefundRule
                                        )) || void 0 === t
                                        ? void 0
                                        : t.length) > 0
                                    );
                                  })) || []),
                            (s =
                              null == a
                                ? void 0
                                : a.map(function (e) {
                                    return (0, c.A)(
                                      (0, c.A)({}, e),
                                      {},
                                      {
                                        name: e.commoditySkuName,
                                        refund: e.transformRefundRule,
                                      }
                                    );
                                  })),
                            (l =
                              null == s
                                ? void 0
                                : s.reduce(function (e, t) {
                                    var n;
                                    return (
                                      (null !==
                                        (n = e.filter(function (e) {
                                          return (
                                            e.commoditySkuId ===
                                            t.commoditySkuId
                                          );
                                        })) &&
                                        void 0 !== n &&
                                        n.length) ||
                                        e.push(t),
                                      e
                                    );
                                  }, [])),
                            Pe({
                              type: "OrderSiteModel/saveFundVisible",
                              payload: !(!l || !l.length) && l,
                            }),
                            !l || !l.length)
                          ) {
                            o.next = 16;
                            break;
                          }
                          return o.abrupt("return");
                        case 16:
                          return ot(), o.abrupt("return");
                        case 18:
                          ot();
                        case 19:
                        case "end":
                          return o.stop();
                      }
                  }, _callee4);
                })
              ),
              [null == Le ? void 0 : Le.stadiumId, ne]
            ),
            lt = (function () {
              var e = (0, s.A)(
                (0, o.A)().mark(function _callee5() {
                  var e, t, n, i, a, s, l, c, d, u;
                  return (0, o.A)().wrap(
                    function _callee5$(o) {
                      for (;;)
                        switch ((o.prev = o.next)) {
                          case 0:
                            if (
                              ((e = C()().format("DD")),
                              (t = Z ? Z.num : je.today),
                              e != t)
                            ) {
                              o.next = 27;
                              break;
                            }
                            return (
                              (n = C()().format("HH:mm")),
                              (o.next = 6),
                              (0, ln.Hh)()
                            );
                          case 6:
                            (i = o.sent).isSuccess &&
                              (n = C()(
                                null === (a = i.data) || void 0 === a
                                  ? void 0
                                  : a.result
                              ).format("HH:mm")),
                              (s = (0, r.A)(ne)),
                              (o.prev = 9),
                              s.s();
                          case 11:
                            if ((l = s.n()).done) {
                              o.next = 19;
                              break;
                            }
                            if (
                              ((c = l.value),
                              (d = c.endTime),
                              (u = c.startTime),
                              !L(n, u, d))
                            ) {
                              o.next = 17;
                              break;
                            }
                            return M(!0), o.abrupt("return");
                          case 17:
                            o.next = 11;
                            break;
                          case 19:
                            o.next = 24;
                            break;
                          case 21:
                            (o.prev = 21), (o.t0 = o.catch(9)), s.e(o.t0);
                          case 24:
                            return (o.prev = 24), s.f(), o.finish(24);
                          case 27:
                            st();
                          case 28:
                          case "end":
                            return o.stop();
                        }
                    },
                    _callee5,
                    null,
                    [[9, 21, 24, 27]]
                  );
                })
              );
              return function handleOrderPre() {
                return e.apply(this, arguments);
              };
            })(),
            ssu_do = (function () {
              console.log("SSU do build");
              let ssu_do_count = 0;
              const courtItems = Ye.current || [];
              const reserveId2CountItemObj = courtItems.reduce((acc, cur) => {
                acc[cur.reserveId] = { ...cur };
                return acc;
              }, {});
              const createOrderResultObj = {};
              const updateCountItem = (createOrderParams, msg) => {
                (
                  createOrderParams?.data?.businessInfo?.reserveIds || []
                ).forEach((reserveId) => {
                  // 只赋值一次，避免成功再抢就重复预定了
                  if (!reserveId2CountItemObj[reserveId].result) {
                    reserveId2CountItemObj[reserveId].result = msg;
                  }
                });
                console.log(
                  "SSU updateCountItem",
                  `抢票结果：\n${Object.keys(reserveId2CountItemObj)
                    .map(
                      (reserveId, index) =>
                        `${index + 1}. ${
                          reserveId2CountItemObj[reserveId].name
                        }-${reserveId2CountItemObj[reserveId].startTime}-${
                          reserveId2CountItemObj[reserveId].endTime
                        }: ${reserveId2CountItemObj[reserveId].result}`
                    )
                    .join("\n")}`,
                  {
                    createOrderParams,
                    msg,
                    reserveId2CountItemObj,
                  }
                );
                if (
                  Object.keys(reserveId2CountItemObj).every(
                    (reserveId) => reserveId2CountItemObj[reserveId].result
                  )
                ) {
                  alert(
                    `抢票结果：\n${Object.keys(reserveId2CountItemObj)
                      .map(
                        (reserveId, index) =>
                          `${index + 1}. ${
                            reserveId2CountItemObj[reserveId].name
                          }-${reserveId2CountItemObj[reserveId].startTime}-${
                            reserveId2CountItemObj[reserveId].endTime
                          }: ${reserveId2CountItemObj[reserveId].result}`
                      )
                      .join("\n")}`
                  );
                }
              };
              const buildCreateOrderParamsKey = (createOrderParams) =>
                (createOrderParams?.data?.businessInfo?.reserveIds || []).join(
                  "-"
                );
              // 默认取用户选中场地
              return async function ({
                createOrderParamsArray = [],
                commodityIds = [],
                discountItems = [],
                isAlert = true,
              } = {}) {
                const { stadiumId } = Le;
                for (let i = 0; i < createOrderParamsArray.length; i++) {
                  const createOrderParams = createOrderParamsArray[i];

                  const createOrderParamsKey =
                    buildCreateOrderParamsKey(createOrderParams);
                  if (createOrderResultObj[createOrderParamsKey]) {
                    window.ssu_setTitle(
                      `${createOrderParamsKey} 已有抢订结果【${createOrderResultObj[createOrderParamsKey]}】，跳过重复抢订`
                    );
                    break;
                  }

                  const requestData = {
                    index: ++ssu_do_count,
                    key: createOrderParamsKey,
                    createOrderParams,
                    startTime: ssu_curFormartDateHMSMS(),
                  };

                  window
                    ._ssu_request(createOrderParams)
                    .then((orderData) => {
                      const { order } = orderData?.data || {};
                      console.log("SSU _ssu_request order data", orderData);
                      requestData.endTime = ssu_curFormartDateHMSMS();
                      requestData.data = orderData;
                      if (order && order.orderId) {
                        const msg = "💯抢单成功 " + order.orderId + "💯";
                        createOrderResultObj[createOrderParamsKey] = msg;
                        updateCountItem(createOrderParams, msg);
                        window.ssu_setTitle(
                          `Step4: ssu_do order 🛑【${requestData.index}】🛑 ${
                            requestData.key
                          }（${requestData.startTime}-${
                            requestData.endTime
                          }） ${msg}, params=${JSON.stringify(requestData)}`
                        );
                        let payRetryCount = 3;
                        const handleRetryPay = (err) => {
                          payRetryCount--;
                          window.ssu_setTitle(
                            `Step5: 支付失败 ${err}，可重试次数${payRetryCount}`
                          );
                          if (payRetryCount > 0) {
                            // 直接重试支付，让出系统资源高优支持创建订单
                            setTimeout(handlePay, 0);
                          }
                        };
                        const handlePay = () => {
                          // 支付成功
                          const payPramas = {
                            api: "mtop.sports.centre.c.trade.pay.startCardPay",
                            v: "1.0",
                            type: "POST",
                            data: {
                              orderId: order.orderId,
                              franchiseCompanyId: order.subOrders[0]
                                ? order.subOrders[0].companyId
                                : "",
                              selectedDiscountItems: JSON.stringify(
                                discountItems.filter((discountItem) =>
                                  (commodityIds || []).some(
                                    (commodityId) =>
                                      commodityId &&
                                      discountItem.containSkuList.includes(
                                        commodityId
                                      )
                                  )
                                )
                              ),
                              cardId: discountItems[0]
                                ? discountItems[0].code
                                : "",
                            },
                          };
                          console.log("SSU__2 pay pramas", payPramas);
                          window
                            ._ssu_request(payPramas)
                            .then((payData) => {
                              window.ssu_setTitle(
                                `Step4: ssu_do pay params=${JSON.stringify(
                                  payPramas
                                )}, payData=${payData.data}`
                              );
                              const { payment, orderId } = payData.data || {};
                              if (payment && orderId) {
                                const title = `🛑【${requestData.index}】🛑 ${
                                  requestData.key
                                }（${requestData.startTime}-${
                                  requestData.endTime
                                }） 支付成功 ${payment / 100}元 ${orderId}`;
                                window.ssu_setTitle(title);
                                // alert(title);
                                return { success: payPramas };
                              } else {
                                handleRetryPay(`payData异常 ${payData.data}`);
                              }
                            })
                            .catch((err) => {
                              handleRetryPay(err);
                            });
                        };
                        // 5s后支付，让出系统资源高优支持创建订单
                        setTimeout(handlePay, 5000);
                      } else {
                        const error =
                          "⭕️抢单失败 " + orderData.message + "⭕️";
                        if (error.includes("已被预订")) {
                          // 已被预订则不再无效重试
                          createOrderResultObj[createOrderParamsKey] = error;
                          updateCountItem(createOrderParams, error);
                          window.ssu_setTitle(
                            `Step4: ssu_do order 🛑【${requestData.index}】🛑 ${
                              requestData.key
                            }（${requestData.startTime}-${
                              requestData.endTime
                            }） ${error} 【标记不再抢】, params=${JSON.stringify(
                              requestData
                            )}`
                          );
                        } else {
                          window.ssu_setTitle(
                            `Step4: ssu_do order 🛑【${requestData.index}】🛑 ${
                              requestData.startTime
                            }-${
                              requestData.endTime
                            } ${error}, params=${JSON.stringify(requestData)}`
                          );
                        }
                        // 支付失败
                        window.ssu_setTitle(error);
                        // if (
                        //   !confirm(`${title}，确定继续抢，取消刷新当前页？`)
                        // ) {
                        //   location.reload();
                        // }
                        return { error: "order empty" };
                      }
                    })
                    .catch((err) => {
                      const msg = "异常 " + err;
                      createOrderResultObj[createOrderParamsKey] = msg;
                      updateCountItem(createOrderParams, msg);
                      requestData.endTime = ssu_curFormartDateHMSMS();
                      requestData.err = err;
                      window.ssu_setTitle(
                        `❌抢单异常 🛑${requestData.index}🛑 ${
                          requestData.key
                        }（${requestData.startTime}-${
                          requestData.endTime
                        }） ${err}❌ , params=${JSON.stringify(requestData)}`
                      );
                    });
                }
              };
            })(),
            dt = (0, V.useMemo)(
              function () {
                return null != pe && pe.timeSlot && Array.isArray(pe.timeSlot)
                  ? O.replace(/(^[\d.]+)(.+)$/, function (e, t, n) {
                      var i;
                      return (
                        t *
                          (null == pe ||
                          null === (i = pe.timeSlot) ||
                          void 0 === i
                            ? void 0
                            : i.length) +
                        n
                      );
                    })
                  : null;
              },
              [null == pe ? void 0 : pe.timeSlot]
            );
          return (0, W.jsx)(W.Fragment, {
            children: (0, W.jsx)(b.Ss, {
              className: cn,
              children:
                null != Le && Le.stadiumId && null != Le && Le.propertyValueId
                  ? (0, W.jsxs)(W.Fragment, {
                      children: [
                        (0, W.jsx)(b.Ss, {
                          children: (0, W.jsx)(xe, {
                            currentDate: Fe,
                            onClickWeekItem: qe,
                            selectDay: Z,
                            TimeListId: De,
                          }),
                        }),
                        oe && !1,
                        (0, W.jsx)(b.Ss, {
                          children: (0, W.jsx)(Pt, {
                            sellTimeRemind:
                              null == pe ? void 0 : pe.sellTimeRemind,
                          }),
                        }),
                        (0, W.jsx)(b.Ss, {
                          children: (0, W.jsx)(Tt, {
                            info: Ee,
                          }),
                        }),
                        (0, W.jsx)(b.eB, {
                          children:
                            fe || ge || be
                              ? (0, W.jsx)(Ke, {})
                              : ue &&
                                pe &&
                                null != ue &&
                                ue.length &&
                                null !== (e = Object.keys(pe || {})) &&
                                void 0 !== e &&
                                e.length &&
                                null != ue &&
                                null !==
                                  (t = ue.filter(function (e) {
                                    var t;
                                    return null == e ||
                                      null === (t = e.rowList) ||
                                      void 0 === t
                                      ? void 0
                                      : t.length;
                                  })) &&
                                void 0 !== t &&
                                t.length
                              ? (0, W.jsxs)(b.eB, {
                                  children: [
                                    (0, W.jsxs)(b.Ss, {
                                      className: dn,
                                      children: [
                                        (0, W.jsx)(b.Ss, {
                                          className: un,
                                        }),
                                        (0, W.jsx)(pt, {
                                          info: Ee,
                                        }),
                                        (0, W.jsx)(b.BM, {
                                          className: pn,
                                          scrollX: !0,
                                          scrollWithAnimation: !0,
                                          id: "gymListScrollView_self",
                                          showScrollbar: !1,
                                          enhanced: !0,
                                          children: (0, W.jsx)(b.Ss, {
                                            className: hn,
                                            children:
                                              null == ue
                                                ? void 0
                                                : ue
                                                    .filter(function (e) {
                                                      var t;
                                                      return null ===
                                                        (t = e.rowList) ||
                                                        void 0 === t
                                                        ? void 0
                                                        : t.length;
                                                    })
                                                    .map(function (e, t) {
                                                      return (0, W.jsx)(
                                                        b.Ss,
                                                        {
                                                          className: mn,
                                                          children: (0, W.jsx)(
                                                            b.EY,
                                                            {
                                                              className: fn,
                                                              children: e.name,
                                                            }
                                                          ),
                                                        },
                                                        ""
                                                          .concat(e.name, "-")
                                                          .concat(t)
                                                      );
                                                    }),
                                          }),
                                        }),
                                      ],
                                    }),
                                    (0, W.jsx)(b.BM, {
                                      className: vn,
                                      scrollY: !0,
                                      showScrollbar: !1,
                                      enhanced: !0,
                                      scrollWithAnimation: !0,
                                      fastDeceleration: !0,
                                      scrollAnchoring: !0,
                                      scrollIntoView: ke,
                                      bounces: !0,
                                      children: (0, W.jsxs)(b.Ss, {
                                        className: gn,
                                        children: [
                                          (0, W.jsx)(b.Ss, {
                                            className: wn,
                                            style: {
                                              marginTop: Y(20),
                                              height: dt,
                                            },
                                            children:
                                              null == pe ||
                                              null === (n = pe.timeSlot) ||
                                              void 0 === n
                                                ? void 0
                                                : n.map(function (e, t) {
                                                    var n = e
                                                      ? String(e).replace(
                                                          ":",
                                                          ""
                                                        )
                                                      : null;
                                                    return (0, W.jsxs)(
                                                      b.Ss,
                                                      {
                                                        className: bn,
                                                        id: "ele".concat(n),
                                                        style: {
                                                          height: F(),
                                                        },
                                                        children: [
                                                          (0, W.jsx)(b.Ss, {
                                                            className: yn,
                                                            children: e,
                                                          }),
                                                          (0, W.jsx)(b.Ss, {
                                                            className: An,
                                                            style: {
                                                              right: "".concat(
                                                                D
                                                              ),
                                                              width: "".concat(
                                                                z
                                                              ),
                                                              height: "".concat(
                                                                j
                                                              ),
                                                            },
                                                          }),
                                                          t ===
                                                          pe.timeSlot.length - 1
                                                            ? null
                                                            : (0, W.jsx)(b.Ss, {
                                                                className: xn,
                                                                style: {
                                                                  right: B,
                                                                  height:
                                                                    "100%",
                                                                },
                                                              }),
                                                        ],
                                                      },
                                                      e
                                                    );
                                                  }),
                                          }),
                                          (0, W.jsx)(b.BM, {
                                            className: _n,
                                            scrollX: !0,
                                            scrollWithAnimation: !0,
                                            ref: q,
                                            showScrollbar: !1,
                                            style: {
                                              height: dt,
                                            },
                                            id: "pageContentScroolViewGymListId",
                                            onScroll: function _handleScroll() {
                                              (0, h._Y)()
                                                .select(
                                                  "#pageContentScroolViewGymListIdView"
                                                )
                                                .boundingClientRect()
                                                .selectViewport()
                                                .scrollOffset()
                                                .exec(function (e) {
                                                  var t =
                                                    65 - (e[0] || {}).left;
                                                  (0, h._Y)()
                                                    .select(
                                                      "#gymListScrollView_self"
                                                    )
                                                    .node()
                                                    .exec(function (e) {
                                                      e[0].node.scrollTo({
                                                        left: t,
                                                      });
                                                    });
                                                });
                                            },
                                            children: (0, W.jsx)(b.Ss, {
                                              className: En,
                                              id: "pageContentScroolViewGymListIdView",
                                              children: (0, W.jsx)(K, {
                                                List: ue,
                                                onSelect: tt,
                                                selectList: ne,
                                                onRemove: nt,
                                              }),
                                            }),
                                          }),
                                        ],
                                      }),
                                    }),
                                    (0, W.jsxs)(b.Ss, {
                                      className: Sn,
                                      children: [
                                        (0, W.jsx)(Ze, {
                                          selectList: ne,
                                          isCollasp: g,
                                          renderSelectList: Ge,
                                          onSetCollasp: Qe,
                                          selectDay: it.selectDay,
                                          onSelect: tt,
                                        }),
                                        (0, W.jsx)(Jt, {
                                          data: Xe,
                                          onConfirm: ot,
                                        }),
                                        (0, W.jsxs)(b.Ss, {
                                          className: Cn,
                                          children: [
                                            (0, W.jsx)(b.Ss, {
                                              className: Tn,
                                              children: _
                                                ? (0, W.jsx)(b.Ss, {
                                                    className: Mn,
                                                    children: "计算中...",
                                                  })
                                                : (0, W.jsxs)(W.Fragment, {
                                                    children: [
                                                      (0, W.jsxs)(b.Ss, {
                                                        className: kn,
                                                        children: [
                                                          "¥ ",
                                                          R.discountedPrice ||
                                                            0,
                                                        ],
                                                      }),
                                                      R.discountedPrice !==
                                                      R.originPrice
                                                        ? (0, W.jsxs)(b.Ss, {
                                                            className: In,
                                                            children: [
                                                              "¥",
                                                              R.originPrice,
                                                            ],
                                                          })
                                                        : null,
                                                    ],
                                                  }),
                                            }),
                                            (0, W.jsx)(at.A, {
                                              throttleTime: 2e3,
                                              onClick: async function onClick({
                                                isPassConfirm = false,
                                              }) {
                                                window.ssu_float_btn.style.display =
                                                  "block";
                                                window.ssu_content_area.style.display =
                                                  "block";
                                                window.ssu_setTitle(
                                                  `Hello SSU!`
                                                );
                                                const [
                                                  startHour,
                                                  startMinute,
                                                  startSecond,
                                                ] = [7, 0, 0];
                                                // SSU TODO
                                                // ] = [6, 53, 0];
                                                // 炮火延长毫秒数
                                                // SSU TODO
                                                const extendedFireMSTime = 3000;
                                                // const extendedFireMSTime = 1000;
                                                // 构建当前时间和目标时间的Date对象
                                                const now = new Date();
                                                const startTime = new Date(
                                                  now.getFullYear(),
                                                  now.getMonth(),
                                                  now.getDate(),
                                                  startHour,
                                                  startMinute,
                                                  startSecond
                                                );
                                                const targetTime = new Date(
                                                  startTime.getTime()
                                                );

                                                // 炮火准备
                                                const ssu_fire_ready =
                                                  async function (params) {
                                                    // 步骤一：前置获取折扣数据存储，这个一次就可以
                                                    const courtItems =
                                                      Ye.current || [];
                                                    const { stadiumId } = Le;
                                                    const commoditySkuIds =
                                                      JSON.stringify(
                                                        (courtItems || []).map(
                                                          (courtItem) =>
                                                            courtItem.commodityId
                                                        )
                                                      );
                                                    const originalPrice = (
                                                      courtItems || []
                                                    ).reduce(
                                                      (acc, cur) =>
                                                        acc + cur.originPrice,
                                                      0
                                                    );
                                                    // curl -X GET "https://h5api.m.taobao.com/h5/mtop.sports.centre.c.trade.discount.gettotaldiscountitems/1.0/2.0/?jsv=2.4.12&appKey=12574478&t=1735045741296&sign=15b5f320806a7504c7145b613cda4224&c=a045feaf25dbac55f16e857195f07d06_1735044450789%3B20a47af2296c01ec32c8b5af3c8d4f0c&dataType=json&api=mtop.sports.centre.c.trade.discount.getTotalDiscountItems&v=1.0&type=originaljson&data=%7B%22queryType%22%3A%22IN_ORDER%22%2C%22stadiumId%22%3A2158%2C%22commoditySkuIds%22%3A%22%5B%5C%222024061113163560844158920%5C%22%5D%22%2C%22originalPrice%22%3A8000%7D" -H "Host: h5api.m.taobao.com" -H "Connection: keep-alive" -H "x-centre-token: pQrA_O9syyLUT38Ggv255LT5jvd7hIAA8BqUHlzF7do=" -H "ssoToken: " -H "appid: wx447e7b2dfdcedc89" -H "x-tap: wx" -H "content-type: application/x-www-form-urlencoded" -H "Accept: application/json" -H "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 13_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.50(0x1800323d) NetType/WIFI Language/zh_CN" -H "Referer: https://servicewechat.com/wx447e7b2dfdcedc89/227/page-frame.html"
                                                    // {"queryType":"IN_ORDER","stadiumId":2158,"commoditySkuIds":"[\"2024061113163560844158920\"]","originalPrice":8000}
                                                    const discountParams = {
                                                      api: "mtop.sports.centre.c.trade.discount.getTotalDiscountItems",
                                                      v: "1.0",
                                                      type: "POST",
                                                      data: {
                                                        queryType: "IN_ORDER",
                                                        stadiumId: +stadiumId,
                                                        commoditySkuIds,
                                                        originalPrice,
                                                      },
                                                    };
                                                    console.log(
                                                      "SSU Step1: ssu_fire_ready discount discountParams",
                                                      discountParams,
                                                      courtItems
                                                    );
                                                    const discountStartTime =
                                                      new Date().getTime();
                                                    const discountData =
                                                      await window._ssu_request(
                                                        discountParams
                                                      );
                                                    const discountEndTime =
                                                      new Date().getTime();
                                                    const {
                                                      discountItems = [],
                                                    } = discountData.data || {};
                                                    // 2/3网络时间，为了提前进入排队重试
                                                    const netTime =
                                                      ((discountEndTime -
                                                        discountStartTime) *
                                                        2) /
                                                      3;
                                                    startTime.setMilliseconds(
                                                      startTime.getMilliseconds() -
                                                        netTime
                                                    );
                                                    // 累加炮火延长时间
                                                    targetTime.setMilliseconds(
                                                      targetTime.getMilliseconds() +
                                                        extendedFireMSTime
                                                    );
                                                    console.log(
                                                      "SSU Step1: ssu_fire_ready discountItems",
                                                      {
                                                        discountStartTime,
                                                        discountStartTime,
                                                        netTime,
                                                      },
                                                      discountItems
                                                    );

                                                    window.ssu_setTitle(
                                                      `丈量网络单程耗时${netTime}，折扣信息  ${JSON.stringify(
                                                        discountItems
                                                      )}`
                                                    );

                                                    // 步骤二：片场连续分组
                                                    const courtItemGroupedByName =
                                                      courtItems.reduce(
                                                        (acc, item) => {
                                                          const key = `${item.name}-${item.startTime}`;
                                                          acc[key] = item;
                                                          return acc;
                                                        },
                                                        {}
                                                      );

                                                    console.log(
                                                      "SSU Step1: ssu_fire_ready groupedByName ",
                                                      courtItemGroupedByName
                                                    );

                                                    // 获取所有键（name）并排序
                                                    const courtItemSortedKeys =
                                                      Object.keys(
                                                        courtItemGroupedByName
                                                      ).sort();

                                                    // 根据排序后的键生成排序后的数组
                                                    const courtItemSortedArray =
                                                      courtItemSortedKeys.map(
                                                        (key) =>
                                                          courtItemGroupedByName[
                                                            key
                                                          ]
                                                      );

                                                    console.log(
                                                      "SSU Step1: ssu_fire_ready sortedArray ",
                                                      courtItemSortedArray
                                                    );

                                                    const courtItemsArray = [];
                                                    // 每个场地独立订，方便退
                                                    // 确保 sortedArray 的长度至少为6

                                                    // 顺序取出6个元素组成新数组
                                                    for (
                                                      let i = 0;
                                                      i <
                                                      courtItemSortedArray.length;
                                                      i++
                                                    ) {
                                                      // 按同场地放入一个分组
                                                      const sameItemNameLastIndex =
                                                        courtItemSortedArray.findLastIndex(
                                                          (item) =>
                                                            item.name ===
                                                            courtItemSortedArray[
                                                              i
                                                            ].name
                                                        );
                                                      console.log(
                                                        "SSU courtItemSortedArray 分组",
                                                        `${courtItemSortedArray[i].name}-${courtItemSortedArray[i].startTime} ～ ${courtItemSortedArray[sameItemNameLastIndex].name}-${courtItemSortedArray[sameItemNameLastIndex].startTime}`
                                                      );
                                                      const newCourtItems =
                                                        courtItemSortedArray.slice(
                                                          i,
                                                          sameItemNameLastIndex +
                                                            1
                                                        );
                                                      courtItemsArray.push(
                                                        newCourtItems
                                                      );
                                                      i = sameItemNameLastIndex;
                                                    }

                                                    // console.log(
                                                    //   `SSU ssu_do() ${window.ssu_formartDateHMSMS(
                                                    //     new Date().getTime()
                                                    //   )}`,
                                                    //   ++ssu_do_count,
                                                    //   {
                                                    //     courtItemsArray,
                                                    //     discountItems,
                                                    //     isAlert,
                                                    //   }
                                                    // );

                                                    const createOrderParamsArray =
                                                      [];
                                                    const createOrderNativeParamsArray =
                                                      [];
                                                    const commodityId2AmountObj =
                                                      {};
                                                    // 使用 forEach 遍历 sortedArray
                                                    for (
                                                      let i = 0;
                                                      i <
                                                      courtItemsArray.length;
                                                      i++
                                                    ) {
                                                      const courtItems =
                                                        courtItemsArray[i];
                                                      // console.log(
                                                      //   `SSU__2 ssu do every ${i + 1}/${courtItemsArray.length}`,
                                                      //   courtItems
                                                      // );
                                                      const totalAmount =
                                                        courtItems.reduce(
                                                          (acc, cur) =>
                                                            acc +
                                                            cur.originPrice,
                                                          0
                                                        );
                                                      const mainTotalAmount =
                                                        totalAmount;

                                                      const mainTotalPayment =
                                                        courtItems.reduce(
                                                          (acc, cur) => {
                                                            // 10000 表示没有折扣
                                                            const discountMother = 10000;
                                                            // 找到对应场次的折扣信息，如 5.7折对应"discountValue": 7500
                                                            const discountValue =
                                                              (
                                                                discountItems.find(
                                                                  (
                                                                    discountItem
                                                                  ) =>
                                                                    discountItem.containSkuList.includes(
                                                                      cur.commodityId
                                                                    )
                                                                ) || {}
                                                              ).discountValue ||
                                                              discountMother;
                                                            if (
                                                              !commodityId2AmountObj[
                                                                cur.commodityId
                                                              ]
                                                            ) {
                                                              commodityId2AmountObj[
                                                                cur.commodityId
                                                              ] = 1;
                                                            } else {
                                                              commodityId2AmountObj[
                                                                cur.commodityId
                                                              ]++;
                                                            }
                                                            return (
                                                              acc +
                                                              (cur.originPrice *
                                                                discountValue) /
                                                                discountMother
                                                            );
                                                          },
                                                          0
                                                        );
                                                      const payment =
                                                        mainTotalPayment;
                                                      const commodityIds =
                                                        Object.keys(
                                                          commodityId2AmountObj
                                                        );
                                                      const requestItems =
                                                        commodityIds.map(
                                                          (commodityId) => ({
                                                            commoditySkuId:
                                                              commodityId,
                                                            amount:
                                                              commodityId2AmountObj[
                                                                commodityId
                                                              ],
                                                          })
                                                        );
                                                      // console.log("SSU___", Ye, Le, Fe);
                                                      const reserveIds =
                                                        courtItems.map(
                                                          (court) =>
                                                            court.reserveId
                                                        );
                                                      const [
                                                        startDate,
                                                        endDate,
                                                      ] = [
                                                        Fe.current,
                                                        Fe.current,
                                                      ];
                                                      const reserveItems =
                                                        courtItems.map(
                                                          (court) => ({
                                                            reserveCalculateType:
                                                              "PIECE_ORDER",
                                                            commoditySkuId:
                                                              court.commodityId,
                                                            reservePrice:
                                                              court.discountedPrice,
                                                            reserveId:
                                                              court.reserveId,
                                                            startDate,
                                                            endDate,
                                                            startTime:
                                                              court.startTime,
                                                            endTime:
                                                              court.endTime,
                                                          })
                                                        );
                                                      const createOrderParams =
                                                        {
                                                          api: "mtop.sports.centre.c.trade.order.create",
                                                          v: "2.0",
                                                          type: "POST",
                                                          data: {
                                                            stadiumId,
                                                            totalAmount,
                                                            mainTotalAmount,
                                                            mainTotalPayment,
                                                            payment,
                                                            discountItems:
                                                              JSON.stringify(
                                                                discountItems
                                                              ),
                                                            requestItems,
                                                            reserveItems,
                                                            contactMobile:
                                                              window.ssu_mobile,
                                                            businessInfo: {
                                                              reserveIds:
                                                                reserveIds,
                                                            },
                                                            disPoint:
                                                              "a21i0.brand_miniapp_order_site.order_view.order_btn",
                                                          },
                                                        };
                                                      createOrderParamsArray.push(
                                                        createOrderParams
                                                      );
                                                      const createOrderNativeParams =
                                                        _ssu_buildParams({
                                                          time: startTime.getTime(),
                                                          data: createOrderParams.data,
                                                          api: createOrderParams.api,
                                                        });
                                                      createOrderNativeParamsArray.push(
                                                        createOrderNativeParams
                                                      );
                                                    }

                                                    const commodityIds =
                                                      Object.keys(
                                                        commodityId2AmountObj
                                                      );
                                                    return {
                                                      courtItemsArray,
                                                      discountItems,
                                                      commodityIds,
                                                      netTime,
                                                      createOrderParamsArray,
                                                    };
                                                  };
                                                const {
                                                  courtItemsArray,
                                                  discountItems,
                                                  createOrderParamsArray,
                                                  createOrderNativeParamsArray,
                                                  netTime,
                                                  commodityIds,
                                                } = await ssu_fire_ready();
                                                const confirmMsg = `抢${
                                                  courtItemsArray.length
                                                }分片场地:\n${courtItemsArray
                                                  .map((courtItems, index) => {
                                                    return `${
                                                      index + 1
                                                    }. ${courtItems
                                                      .map(
                                                        (courtItem) =>
                                                          `${courtItem.name}-${courtItem.startTime}`
                                                      )
                                                      .join("、")}`;
                                                  })
                                                  .join("\n")}\n折扣:\n${(
                                                  discountItems || []
                                                )
                                                  .map(
                                                    (discountItem, index) =>
                                                      `${index + 1}. ${
                                                        discountItem.discountValue /
                                                        1000
                                                      } ${
                                                        "折"
                                                        // discountItem.calculateType
                                                      }`
                                                  )
                                                  .join("\n" || "1. 无")}`;
                                                console.log(
                                                  "SSU Step1: ssu_fire_ready result",
                                                  confirmMsg
                                                );
                                                window.ssu_setTitle(
                                                  "炮火就绪 " + confirmMsg
                                                );
                                                window.ssu_setTitle(
                                                  "分批炮火清点 " +
                                                    JSON.stringify(
                                                      createOrderParamsArray
                                                    )
                                                );
                                                window.ssu_setTitle(
                                                  "分批Native炮火清点 " +
                                                    JSON.stringify(
                                                      createOrderNativeParamsArray
                                                    )
                                                );
                                                if (window.is_ssu_vip) {
                                                  if (
                                                    window.ssu_vip_intervalId1
                                                  ) {
                                                    window.ssu_setTitle(
                                                      "炮火就绪中..."
                                                    );
                                                    return;
                                                  }
                                                  // 当前时间超过7点0分5秒
                                                  function isExceedecFireTime() {
                                                    const now = new Date();
                                                    // 比较当前时间和目标时间
                                                    const result =
                                                      now >= targetTime;
                                                    const msg = `SSU Step3: ssu_fire start: isExceedecFireTime, ${JSON.stringify(
                                                      {
                                                        now: window.ssu_formartDateHMSMS(
                                                          now.getTime()
                                                        ),
                                                        targetTime:
                                                          window.ssu_formartDateHMSMS(
                                                            targetTime.getTime()
                                                          ),
                                                        result,
                                                      }
                                                    )}`;
                                                    // console.log(msg);
                                                    window.ssu_setTitle(msg);
                                                    return result;
                                                  }
                                                  const delay =
                                                    startTime.getTime() -
                                                    now.getTime();

                                                  console.log(
                                                    `SSU ${window.ssu_formartDateHMSMS(
                                                      new Date().getTime()
                                                    )} Step2: ssu_fire go 弹弹窗`
                                                  );
                                                  window.ssu_setTitle(
                                                    "Step2: ssu_fire go 弹弹窗"
                                                  );
                                                  const startTimeFormatStr =
                                                    window.ssu_formartDateHMSMS(
                                                      startTime.getTime()
                                                    );
                                                  // 超过start时间直接抢，否则确认倒计时
                                                  const confirmMsg2 = `${confirmMsg}。\n你确定 ${startTimeFormatStr} - ${window.ssu_formartDateHMSMS(
                                                    targetTime.getTime()
                                                  )} 时段抢吗（点击取消立即抢）？`;
                                                  window.ssu_setTitle(
                                                    `Step2: ssu_fire go, delay=${delay}, isPassConfirm=${isPassConfirm}, ${confirmMsg2} `
                                                  );
                                                  if (
                                                    delay <= 0 ||
                                                    isPassConfirm ||
                                                    confirm(confirmMsg2)
                                                  ) {
                                                    // SSU TODO
                                                    // Native 接管分支
                                                    // if (SSU) {
                                                    //   SSU.request(createOrderNativeParamsArray, startTime);
                                                    //   return;
                                                    // }
                                                    const countDownStep = 100;
                                                    const computeLastTime =
                                                      () => {
                                                        return (
                                                          startTime.getTime() -
                                                          new Date().getTime()
                                                        );
                                                      };
                                                    const readyAndFire = () => {
                                                      let lastTime =
                                                        computeLastTime();
                                                      const formattedTime =
                                                        window.ssu_formartTimeHMSMS(
                                                          lastTime
                                                        );

                                                      window.ssu_setTitle(
                                                        `炮火倒计时 ${formattedTime}`
                                                      );
                                                      // 最后300ms点延时导火索
                                                      if (lastTime <= 300) {
                                                        clearInterval(
                                                          window.ssu_vip_intervalId1
                                                        );
                                                        window.ssu_vip_intervalId1 =
                                                          null;
                                                        let fireWaveCount = 0;
                                                        // 抢票逻辑
                                                        const fire = () => {
                                                          window.ssu_setTitle(
                                                            `开火 🔥🔥🔥X${fireWaveCount}`
                                                          );

                                                          // 直接抢
                                                          const fireCount = 1;
                                                          console.log(
                                                            "SSU Step3: ssu_fire start: fireCount",
                                                            fireCount
                                                          );
                                                          for (
                                                            let i = 0;
                                                            i < fireCount;
                                                            i++
                                                          ) {
                                                            // 通过请求返回值中断瞬时大量请求
                                                            ssu_do({
                                                              createOrderParamsArray,
                                                              commodityIds,
                                                              discountItems,
                                                              isAlert: false,
                                                            });
                                                            console.log(
                                                              `SSU Step3: ssu_fire(${
                                                                i + 1
                                                              }/${fireCount})`
                                                            );
                                                          }
                                                          if (
                                                            isExceedecFireTime()
                                                          ) {
                                                            // 超时重置
                                                            clearInterval(
                                                              window.ssu_vip_intervalId2
                                                            );
                                                            window.ssu_vip_intervalId2 =
                                                              null;
                                                            // if (
                                                            //   confirm(
                                                            //     "继续抢？"
                                                            //   )
                                                            // ) {
                                                            //   onClick({
                                                            //     isPassConfirm: true,
                                                            //   });
                                                            // } else {
                                                            //   location.reload();
                                                            // }
                                                          }
                                                        };
                                                        // 设置当前时延点火触发（导火索）
                                                        lastTime =
                                                          computeLastTime();
                                                        // 提前5ms触发, 增加了网络单程提前，这个前置估算去掉
                                                        const fireDelayOffsetTime = 0;
                                                        const fireDelayTime =
                                                          lastTime -
                                                          fireDelayOffsetTime;
                                                        window.ssu_setTitle(
                                                          `点燃延迟${fireDelayTime}毫秒导火索，${startTimeFormatStr} bingo`
                                                        );
                                                        let hasBingoStarted = false;
                                                        const bingo =
                                                          function () {
                                                            // 确保只触发一次
                                                            if (
                                                              hasBingoStarted
                                                            ) {
                                                              console.log(
                                                                `bingo hasBingoStarted`
                                                              );
                                                              return;
                                                            }
                                                            // 未到时间，继续加到帧动画队列
                                                            if (
                                                              computeLastTime() >
                                                              0
                                                            ) {
                                                              console.log(
                                                                `bingo 未到时间`
                                                              );
                                                              requestAnimationFrame(
                                                                bingo
                                                              );
                                                              return;
                                                            }
                                                            console.log(
                                                              `bingo 💥💥💥`
                                                            );

                                                            hasBingoStarted = true;
                                                            window.ssu_setTitle(
                                                              `💥💥💥`
                                                            );
                                                            fire();
                                                            window.ssu_vip_intervalId2 =
                                                              setInterval(
                                                                () => {
                                                                  fireWaveCount++;
                                                                  fire();
                                                                },
                                                                100
                                                              );
                                                          };
                                                        requestAnimationFrame(
                                                          bingo
                                                        );
                                                        setTimeout(
                                                          bingo,
                                                          fireDelayTime
                                                        );
                                                      }
                                                    };
                                                    readyAndFire();
                                                    window.ssu_vip_intervalId1 =
                                                      setInterval(
                                                        readyAndFire,
                                                        countDownStep
                                                      );
                                                  } else {
                                                    // 用户点击了“取消”
                                                    ssu_do({
                                                      createOrderParamsArray,
                                                      commodityIds,
                                                      discountItems,
                                                    });
                                                  }
                                                } else {
                                                  lt();
                                                }
                                              },
                                              onFirstAppear:
                                                function handleFirstAppear() {
                                                  (0, ae.IC)("EXP", {
                                                    spm_cd: sn.order_btn,
                                                  });

                                                  if (
                                                    window.window.ssu_mobile
                                                  ) {
                                                    window.ssu_setTitle(
                                                      `一键✡︎破➢天机  ${window.window.ssu_mobile}`
                                                    );
                                                  }
                                                },
                                              className: v()(
                                                (0, i.A)(
                                                  (0, i.A)({}, Ln, !0),
                                                  Nn,
                                                  !1 === _ && ne.length
                                                )
                                              ),
                                              children: (0, W.jsx)(b.EY, {
                                                // className: "Pn",
                                                className: window.is_ssu_vip
                                                  ? "ssu_just_do_it"
                                                  : "Pn",
                                                // children: "立即预订",
                                                children: window.is_ssu_vip
                                                  ? // ? "一键✡︎破➢天机"
                                                    "千机一瞬"
                                                  : "立即预订",
                                              }),
                                            }),
                                            He,
                                          ],
                                        }),
                                        (0, W.jsx)(tn, {
                                          show: k,
                                          confirmCallback:
                                            function confirmCallback() {
                                              st(), M(!1);
                                            },
                                          cancelCalllback:
                                            function cancelCalllback() {
                                              return M(!1);
                                            },
                                        }),
                                      ],
                                    }),
                                  ],
                                })
                              : (0, W.jsx)(rt, {}),
                        }),
                      ],
                    })
                  : (0, W.jsx)(se, {
                      stadiumId: null == Le ? void 0 : Le.stadiumId,
                      propertyValueId: null == Le ? void 0 : Le.propertyValueId,
                    }),
            }),
          });
        });
    },
    3272: function (e, t, n) {
      "use strict";
      n.d(t, {
        BM: function () {
          return a;
        },
        LA: function () {
          return s;
        },
        uj: function () {
          return o;
        },
      });
      var i = n(6618),
        r = n(4025),
        a = function setAlipayArrowColor() {
          var e,
            t =
              "#fff" ===
              (arguments.length > 0 && void 0 !== arguments[0]
                ? arguments[0]
                : "#000")
                ? "#ffffff"
                : "#000000";
          null === (e = window) ||
            void 0 === e ||
            null === (e = e.my) ||
            void 0 === e ||
            e.postMessage({
              name: "setNavigationBar",
              color: t,
            });
        },
        o = function hideTitleBar() {
          var e, t;
          if (i.default.isAlipay)
            null === (e = window) ||
              void 0 === e ||
              null === (e = e.AliJSBridge) ||
              void 0 === e ||
              e.callClientBridge("setTransparentTitle", {
                transparentTitle: "always",
              }),
              console.warn("支付宝容器环境设置了手势可以返回"),
              null === (t = window) ||
                void 0 === t ||
                null === (t = t.AlipayJSBridge) ||
                void 0 === t ||
                t.call("setGestureBack", {
                  val: !0,
                });
          else if (i.default.isLedongli) {
            var n;
            null === (n = window) ||
              void 0 === n ||
              n.AliJSBridge.callClientBridge("LeWVJSBridge.setNaviBarHidden", {
                hidden: "1",
              });
          } else if (i.default.isDingTalk) {
            var r;
            null === (r = window) ||
              void 0 === r ||
              r.AliJSBridge.callClientBridge("dd.biz.navigation.hideBar", {
                hidden: !0,
              });
          }
        },
        s = function handleTabbar() {
          if (i.default.isAlipay) {
            var e,
              t = r.Ay.getCurrentPages();
            "/pages/Index/Index/index" ===
            (null === (e = t[t.length - 1]) || void 0 === e ? void 0 : e.route)
              ? my.postMessage({
                  name: "showTab",
                })
              : my.postMessage({
                  name: "hideTab",
                });
          }
        };
    },
    3491: function (e) {
      (function () {
        var t, n, i, r, a, o;
        "undefined" != typeof performance &&
        null !== performance &&
        performance.now
          ? (e.exports = function () {
              return performance.now();
            })
          : "undefined" != typeof process && null !== process && process.hrtime
          ? ((e.exports = function () {
              return (t() - a) / 1e6;
            }),
            (n = process.hrtime),
            (r = (t = function () {
              var e;
              return 1e9 * (e = n())[0] + e[1];
            })()),
            (o = 1e9 * process.uptime()),
            (a = r - o))
          : Date.now
          ? ((e.exports = function () {
              return Date.now() - i;
            }),
            (i = Date.now()))
          : ((e.exports = function () {
              return new Date().getTime() - i;
            }),
            (i = new Date().getTime()));
      }).call(this);
    },
    765: function (e, t, n) {
      for (
        var i = n(3491),
          r = "undefined" == typeof window ? n.g : window,
          a = ["moz", "webkit"],
          o = "AnimationFrame",
          s = r["request" + o],
          l = r["cancel" + o] || r["cancelRequest" + o],
          c = 0;
        !s && c < a.length;
        c++
      )
        (s = r[a[c] + "Request" + o]),
          (l = r[a[c] + "Cancel" + o] || r[a[c] + "CancelRequest" + o]);
      if (!s || !l) {
        var d = 0,
          u = 0,
          p = [],
          h = 1e3 / 60;
        (s = function (e) {
          if (0 === p.length) {
            var t = i(),
              n = Math.max(0, h - (t - d));
            (d = n + t),
              setTimeout(function () {
                var e = p.slice(0);
                p.length = 0;
                for (var t = 0; t < e.length; t++)
                  if (!e[t].cancelled)
                    try {
                      e[t].callback(d);
                    } catch (e) {
                      setTimeout(function () {
                        throw e;
                      }, 0);
                    }
              }, Math.round(n));
          }
          return (
            p.push({
              handle: ++u,
              callback: e,
              cancelled: !1,
            }),
            u
          );
        }),
          (l = function (e) {
            for (var t = 0; t < p.length; t++)
              p[t].handle === e && (p[t].cancelled = !0);
          });
      }
      (e.exports = function (e) {
        return s.call(r, e);
      }),
        (e.exports.cancel = function () {
          l.apply(r, arguments);
        }),
        (e.exports.polyfill = function (e) {
          e || (e = r),
            (e.requestAnimationFrame = s),
            (e.cancelAnimationFrame = l);
        });
    },
    6942: function (e, t) {
      var n;
      !(function () {
        "use strict";
        var i = {}.hasOwnProperty;
        function classNames() {
          for (var e = "", t = 0; t < arguments.length; t++) {
            var n = arguments[t];
            n && (e = appendClass(e, parseValue(n)));
          }
          return e;
        }
        function parseValue(e) {
          if ("string" == typeof e || "number" == typeof e) return e;
          if ("object" != typeof e) return "";
          if (Array.isArray(e)) return classNames.apply(null, e);
          if (
            e.toString !== Object.prototype.toString &&
            !e.toString.toString().includes("[native code]")
          )
            return e.toString();
          var t = "";
          for (var n in e) i.call(e, n) && e[n] && (t = appendClass(t, n));
          return t;
        }
        function appendClass(e, t) {
          return t ? (e ? e + " " + t : e + t) : e;
        }
        e.exports
          ? ((classNames.default = classNames), (e.exports = classNames))
          : void 0 ===
              (n = function () {
                return classNames;
              }.apply(t, [])) || (e.exports = n);
      })();
    },
    8001: function (e, t, n) {
      "use strict";
      function _object_without_properties(e, t) {
        if (null == e) return {};
        var n,
          i,
          r = (function _object_without_properties_loose(e, t) {
            if (null == e) return {};
            var n,
              i,
              r = {},
              a = Object.keys(e);
            for (i = 0; i < a.length; i++)
              (n = a[i]), t.indexOf(n) >= 0 || (r[n] = e[n]);
            return r;
          })(e, t);
        if (Object.getOwnPropertySymbols) {
          var a = Object.getOwnPropertySymbols(e);
          for (i = 0; i < a.length; i++)
            (n = a[i]),
              t.indexOf(n) >= 0 ||
                (Object.prototype.propertyIsEnumerable.call(e, n) &&
                  (r[n] = e[n]));
        }
        return r;
      }
      n.d(t, {
        _: function () {
          return _object_without_properties;
        },
      });
    },
  },
]);
