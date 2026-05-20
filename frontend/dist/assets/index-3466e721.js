import{_ as se,q as re,r as U,o as c,c as N,b as S,d as l,w as a,f as i,g as P,j as x,m as de,$ as ue,a4 as ie,i as s,V as pe,s as I,Y as T,Z as q,B as me,E as fe,a7 as ge,an as _e,a8 as ce,a9 as be,k as ve,l as we,a0 as ye,ao as Ve,W as xe,h as ke,X as Ce,t as F,a1 as Ee,ap as Ue}from"./index-a9e96b91.js";/* empty css             *//* empty css                   *//* empty css                  *//* empty css                     *//* empty css               *//* empty css                        *//* empty css                 *//* empty css                *//* empty css                  *//* empty css               *//* empty css                  *//* empty css                      */import"./el-tooltip-4ed993c7.js";/* empty css                  */import{a as Ne,g as Se,b as Ie,u as $e,c as De,d as Re,t as ze,r as he}from"./system-57617c43.js";const Be={class:"page-container"},Le={class:"card-header"},Me={class:"header-actions"},Pe={__name:"index",setup(Te){const k=i(!1),$=i([]),D=i(0),b=i(!1),v=i(!1),R=i(null),V=i(""),C=i([]),O=i(),w=P({pageNum:1,pageSize:10}),o=P({user:{id:null,username:"",password:"",realName:"",email:"",phone:"",department:""},roleIds:[]});re(async()=>{C.value=(await Ne()).data,y()});async function y(){k.value=!0;try{const n=await Se(w);$.value=n.data.records,D.value=n.data.total}finally{k.value=!1}}function z(n=null){Object.assign(o.user,{id:null,username:"",password:"",realName:"",email:"",phone:"",department:""}),o.roleIds=[],n&&(Object.assign(o.user,n),o.roleIds=C.value.filter(e=>{var r;return(r=n.roles)==null?void 0:r.includes(e.roleCode)}).map(e=>e.id)),b.value=!0}async function j(){o.user.id?await $e(o.user.id,o):await De(o),x.success("保存成功"),b.value=!1,y()}async function A(n){await me.confirm("确定删除该用户？","提示",{type:"warning"}),await Re(n),x.success("删除成功"),y()}async function G(n){await ze(n.id),y()}function H(n){R.value=n.id,V.value="",v.value=!0}async function Y(){await he(R.value,V.value),x.success("密码重置成功"),v.value=!1}async function h(n,e){const r=window.open("","_blank");if(!r){x.warning("浏览器阻止了新窗口，请允许弹窗后重试");return}B(r,e,"正在加载文档...");try{const m=await Ie(n);B(r,e,m.data||"")}catch(m){throw r.close(),m}}function B(n,e,r){n.document.open(),n.document.write(`<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>${E(e)}</title>
  <style>
    body {
      margin: 0;
      background: #f5f7fa;
      color: #1f2a37;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", sans-serif;
    }
    main {
      max-width: 980px;
      margin: 0 auto;
      padding: 32px 24px 48px;
    }
    h1 {
      margin: 0 0 18px;
      font-size: 24px;
      line-height: 1.3;
    }
    pre {
      margin: 0;
      padding: 24px;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      background: #fff;
      color: #263445;
      white-space: pre-wrap;
      word-break: break-word;
      line-height: 1.75;
      font-size: 14px;
      box-sizing: border-box;
    }
  </style>
</head>
<body>
  <main>
    <h1>${E(e)}</h1>
    <pre>${E(r)}</pre>
  </main>
</body>
</html>`),n.document.close()}function E(n){return String(n).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;").replace(/'/g,"&#39;")}const W=n=>({ROLE_ADMIN:"管理员",ROLE_MAINTAINER:"维护员",ROLE_USER:"普通用户"})[n]||n;return(n,e)=>{const r=U("Document"),m=fe,d=de,X=U("Reading"),Z=U("Plus"),u=ge,J=Ee,K=_e,Q=ce,ee=be,le=ue,f=ve,p=we,g=ye,ae=Ue,te=Ve,oe=xe,L=ke,M=ie,ne=Ce;return c(),N("div",Be,[S("div",Le,[e[20]||(e[20]=S("span",{class:"title"},"用户管理",-1)),S("div",Me,[l(d,{plain:"",onClick:e[0]||(e[0]=t=>h("deployment","部署文件"))},{default:a(()=>[l(m,null,{default:a(()=>[l(r)]),_:1}),e[17]||(e[17]=s(" 部署文件 ",-1))]),_:1}),l(d,{plain:"",onClick:e[1]||(e[1]=t=>h("user-guide","用户手册"))},{default:a(()=>[l(m,null,{default:a(()=>[l(X)]),_:1}),e[18]||(e[18]=s(" 用户手册 ",-1))]),_:1}),l(d,{type:"primary",onClick:e[2]||(e[2]=t=>z())},{default:a(()=>[l(m,null,{default:a(()=>[l(Z)]),_:1}),e[19]||(e[19]=s(" 新增用户",-1))]),_:1})])]),l(le,{shadow:"never"},{default:a(()=>[pe((c(),I(Q,{data:$.value,border:"",stripe:""},{default:a(()=>[l(u,{prop:"username",label:"用户名",width:"120"}),l(u,{prop:"realName",label:"姓名",width:"100"}),l(u,{prop:"email",label:"邮箱","min-width":"160"}),l(u,{prop:"phone",label:"手机号",width:"130"}),l(u,{prop:"department",label:"部门",width:"130"}),l(u,{label:"角色",width:"130"},{default:a(({row:t})=>[(c(!0),N(T,null,q(t.roles,_=>(c(),I(J,{key:_,size:"small",style:{margin:"2px"}},{default:a(()=>[s(F(W(_)),1)]),_:2},1024))),128))]),_:1}),l(u,{label:"状态",width:"80"},{default:a(({row:t})=>[l(K,{"model-value":t.status===1,onChange:_=>G(t)},null,8,["model-value","onChange"])]),_:1}),l(u,{prop:"lastLogin",label:"最后登录",width:"160"}),l(u,{label:"操作",width:"180",fixed:"right"},{default:a(({row:t})=>[l(d,{link:"",type:"primary",onClick:_=>z(t)},{default:a(()=>[...e[21]||(e[21]=[s("编辑",-1)])]),_:1},8,["onClick"]),l(d,{link:"",type:"warning",onClick:_=>H(t)},{default:a(()=>[...e[22]||(e[22]=[s("重置密码",-1)])]),_:1},8,["onClick"]),l(d,{link:"",type:"danger",onClick:_=>A(t.id)},{default:a(()=>[...e[23]||(e[23]=[s("删除",-1)])]),_:1},8,["onClick"])]),_:1})]),_:1},8,["data"])),[[ne,k.value]]),l(ee,{"current-page":w.pageNum,"onUpdate:currentPage":e[3]||(e[3]=t=>w.pageNum=t),"page-size":w.pageSize,"onUpdate:pageSize":e[4]||(e[4]=t=>w.pageSize=t),total:D.value,layout:"total, prev, pager, next",class:"pagination",onChange:y},null,8,["current-page","page-size","total"])]),_:1}),l(M,{modelValue:b.value,"onUpdate:modelValue":e[13]||(e[13]=t=>b.value=t),title:o.user.id?"编辑用户":"新增用户",width:"600px","destroy-on-close":""},{footer:a(()=>[l(d,{onClick:e[12]||(e[12]=t=>b.value=!1)},{default:a(()=>[...e[24]||(e[24]=[s("取消",-1)])]),_:1}),l(d,{type:"primary",onClick:j},{default:a(()=>[...e[25]||(e[25]=[s("保存",-1)])]),_:1})]),default:a(()=>[l(L,{ref_key:"formRef",ref:O,model:o.user,"label-width":"90px"},{default:a(()=>[l(oe,{gutter:16},{default:a(()=>[l(g,{span:12},{default:a(()=>[l(p,{label:"用户名",rules:[{required:!0}]},{default:a(()=>[l(f,{modelValue:o.user.username,"onUpdate:modelValue":e[5]||(e[5]=t=>o.user.username=t),disabled:!!o.user.id},null,8,["modelValue","disabled"])]),_:1})]),_:1}),l(g,{span:12},{default:a(()=>[l(p,{label:"密码",rules:o.user.id?[]:[{required:!0}]},{default:a(()=>[l(f,{modelValue:o.user.password,"onUpdate:modelValue":e[6]||(e[6]=t=>o.user.password=t),type:"password",placeholder:o.user.id?"不填则不修改":"请输入密码"},null,8,["modelValue","placeholder"])]),_:1},8,["rules"])]),_:1}),l(g,{span:12},{default:a(()=>[l(p,{label:"姓名"},{default:a(()=>[l(f,{modelValue:o.user.realName,"onUpdate:modelValue":e[7]||(e[7]=t=>o.user.realName=t)},null,8,["modelValue"])]),_:1})]),_:1}),l(g,{span:12},{default:a(()=>[l(p,{label:"邮箱"},{default:a(()=>[l(f,{modelValue:o.user.email,"onUpdate:modelValue":e[8]||(e[8]=t=>o.user.email=t)},null,8,["modelValue"])]),_:1})]),_:1}),l(g,{span:12},{default:a(()=>[l(p,{label:"手机号"},{default:a(()=>[l(f,{modelValue:o.user.phone,"onUpdate:modelValue":e[9]||(e[9]=t=>o.user.phone=t)},null,8,["modelValue"])]),_:1})]),_:1}),l(g,{span:12},{default:a(()=>[l(p,{label:"部门"},{default:a(()=>[l(f,{modelValue:o.user.department,"onUpdate:modelValue":e[10]||(e[10]=t=>o.user.department=t)},null,8,["modelValue"])]),_:1})]),_:1}),l(g,{span:24},{default:a(()=>[l(p,{label:"角色"},{default:a(()=>[l(te,{modelValue:o.roleIds,"onUpdate:modelValue":e[11]||(e[11]=t=>o.roleIds=t)},{default:a(()=>[(c(!0),N(T,null,q(C.value,t=>(c(),I(ae,{key:t.id,label:t.id},{default:a(()=>[s(F(t.roleName),1)]),_:2},1032,["label"]))),128))]),_:1},8,["modelValue"])]),_:1})]),_:1})]),_:1})]),_:1},8,["model"])]),_:1},8,["modelValue","title"]),l(M,{modelValue:v.value,"onUpdate:modelValue":e[16]||(e[16]=t=>v.value=t),title:"重置密码",width:"400px"},{footer:a(()=>[l(d,{onClick:e[15]||(e[15]=t=>v.value=!1)},{default:a(()=>[...e[26]||(e[26]=[s("取消",-1)])]),_:1}),l(d,{type:"primary",onClick:Y},{default:a(()=>[...e[27]||(e[27]=[s("确认重置",-1)])]),_:1})]),default:a(()=>[l(L,{"label-width":"90px"},{default:a(()=>[l(p,{label:"新密码"},{default:a(()=>[l(f,{modelValue:V.value,"onUpdate:modelValue":e[14]||(e[14]=t=>V.value=t),type:"password"},null,8,["modelValue"])]),_:1})]),_:1})]),_:1},8,["modelValue"])])}}},al=se(Pe,[["__scopeId","data-v-32ebc481"]]);export{al as default};
