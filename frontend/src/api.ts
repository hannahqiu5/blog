export type Author={id:string;name:string}; export type Category={id:string;name:string;postCount:number}; export type Tag={id:string;name:string;postCount:number|null};
export type Post={id:string;title:string;content:string;author:Author;category:Category;tags:Tag[];readingTime:number;createdAt:string;updatedAt:string;status:'DRAFT'|'PUBLISHED'};
export type Page<T>={content:T[];totalPages:number;totalElements:number;number:number;size:number;first:boolean;last:boolean;sort:{sorted:boolean;unsorted:boolean;empty:boolean};numberOfElements:number;empty:boolean};
const base=(import.meta.env.VITE_API_BASE_URL||'').replace(/\/$/,'');
export const googleUrl=import.meta.env.VITE_GOOGLE_OAUTH_URL as string|undefined;
export function token(){return localStorage.getItem('blog.jwt')||''}
export function setToken(value:string){localStorage.setItem('blog.jwt',value)}
export function clearToken(){localStorage.removeItem('blog.jwt')}
export function identity(){try{const p=JSON.parse(atob(token().split('.')[1].replace(/-/g,'+').replace(/_/g,'/')));return {email:p.sub||'',id:p.userId||'',admin:(p.roles||p.authorities||[]).some((x:any)=>String(x.authority||x).includes('ADMIN'))}}catch{return {email:'',id:'',admin:false}}}
async function request<T>(path:string,options:RequestInit={}):Promise<T>{const headers=new Headers(options.headers);if(options.body)headers.set('Content-Type','application/json');if(token())headers.set('Authorization',`Bearer ${token()}`);const res=await fetch(`${base}${path}`,{...options,headers});if(!res.ok){let message=`Request failed (${res.status})`;try{const e=await res.json();message=e.message||message;if(e.errors?.length)message+=': '+e.errors.map((x:any)=>x.message).join(', ')}catch{}if(res.status===401){clearToken();window.dispatchEvent(new Event('auth-change'))}throw new Error(message)}if(res.status===204)return undefined as T;return res.json()}
export const api={
 posts:(q:{page:number;size:number;sort:string;keyword:string;categoryId:string;tagId:string})=>{const p=new URLSearchParams({page:String(q.page),size:String(q.size),sort:q.sort});if(q.keyword)p.set('keyword',q.keyword);if(q.categoryId)p.set('categoryId',q.categoryId);if(q.tagId)p.set('tagId',q.tagId);return request<Page<Post>>(`/api/v1/posts?${p}`)},
 post:(id:string)=>request<Post>(`/api/v1/posts/${id}`),categories:()=>request<Category[]>('/api/v1/categories'),tags:()=>request<Tag[]>('/api/v1/tags'),drafts:()=>request<Post[]>('/api/v1/posts/drafts'),
 login:(email:string,password:string)=>request<{jwt:string;expiresIn:number}>('/api/v1/auth/login',{method:'POST',body:JSON.stringify({email,password})}),
 register:(username:string,email:string,password:string)=>request<{id:string;username:string;email:string}>('/api/v1/auth/register',{method:'POST',body:JSON.stringify({username,email,password})}),
 savePost:(data:{id?:string;title:string;content:string;categoryId:string;tagIds:string[];status:'DRAFT'|'PUBLISHED'})=>request<Post>(data.id?`/api/v1/posts/${data.id}`:'/api/v1/posts',{method:data.id?'PUT':'POST',body:JSON.stringify(data)}),
 deletePost:(id:string)=>request<void>(`/api/v1/posts/${id}`,{method:'DELETE'}),createTags:(names:string[])=>request<Tag[]>('/api/v1/tags',{method:'POST',body:JSON.stringify({names})}),createCategory:(name:string)=>request<Category>('/api/v1/categories',{method:'POST',body:JSON.stringify({name})})
};
