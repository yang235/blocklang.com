import { commandFactory, getHeaders } from './utils';
import { NamePayload, DescriptionPayload, IsPublicPayload } from './interfaces';
import { replace } from '@dojo/framework/stores/state/operations';
import { createProcess } from '@dojo/framework/stores/process';
import { ValidateStatus } from '../constant';
import { baseUrl } from '../config';
import { isEmpty } from '../util';

// TODO: 一个字段一个 process vs 一个对象一个 process，哪个更合理？
/************************* new project ****************************/
// 用于设置初始化数据
const startInitForNewProjectCommand = commandFactory(({ path }) => {
	return [
		replace(path('projectParam', 'isPublic'), true),
		replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('projectInputValidation', 'nameErrorMessage'), '')
	];
});

const nameInputCommand = commandFactory<NamePayload>(async ({ path, get, payload: { name } }) => {
	const userName = get(path('user', 'loginName')); // 确保用户必须登录
	const trimedName = name.trim();
	const result = [];

	// 校验是否已填写项目名称
	if (trimedName === '') {
		result.push(replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('projectInputValidation', 'nameErrorMessage'), '项目名称不能为空'));
		return result;
	}

	// 校验项目名称是否符合：字母、数字、中划线(-)、下划线(_)、点(.)
	var regex = /^[a-zA-Z0-9\-\w\.]+$/g;
	if (!regex.test(trimedName)) {
		result.push(replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(
			replace(path('projectInputValidation', 'nameErrorMessage'), '只允许字母、数字、中划线(-)、下划线(_)、点(.)')
		);
		return result;
	}

	// 服务器端校验，校验登录用户下是否存在该项目名
	const response = await fetch(`${baseUrl}/projects/check-name`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			owner: userName,
			name: trimedName
		})
	});
	const json = await response.json();
	if (!response.ok) {
		console.log(response, json);

		result.push(replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('projectInputValidation', 'nameErrorMessage'), json.errors.name));
		return result;
	}

	// 校验通过
	result.push(replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.VALID));
	result.push(replace(path('projectInputValidation', 'nameErrorMessage'), ''));

	result.push(replace(path('projectParam', 'name'), trimedName));
	return result;
});

const descriptionInputCommand = commandFactory<DescriptionPayload>(({ path, payload: { description } }) => {
	return [replace(path('projectParam', 'description'), description.trim())];
});

const isPublicInputCommand = commandFactory<IsPublicPayload>(({ path, payload: { isPublic } }) => {
	return [replace(path('projectParam', 'isPublic'), isPublic)];
});

const saveProjectCommand = commandFactory(async ({ path, get }) => {
	const projectParam = get(path('projectParam'));
	const owner = get(path('user', 'loginName'));

	// 在跳转到新增项目页面时，应设置 isPublic 的初始值为 true
	const response = await fetch(`${baseUrl}/projects`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			owner,
			...projectParam
		})
	});

	const json = await response.json();
	if (!response.ok) {
		// TODO: 在页面上提示保存出错
		console.error(response, json);
		return [replace(path('errors'), json.errors)];
	}

	return [
		replace(path('project'), json),
		// 清空输入参数
		replace(path('projectParam'), undefined),
		replace(path('routing', 'outlet'), 'view-project'),
		replace(path('routing', 'params'), { owner: get(path('user', 'loginName')), project: projectParam.name })
	];
});

/************************* view project ****************************/
export const getProjectCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('project'), {})];
	}

	return [replace(path('project'), json)];
});

const getProjectResourcesCommand = commandFactory(async ({ get, path, payload: { owner, project, pathId = -1 } }) => {
	// 如果没有获取到项目信息，则直接清空项目资源信息
	const projectInfo = get(path('project'));
	console.log(projectInfo);
	if (isEmpty(projectInfo)) {
		return [replace(path('projectResources'), [])];
	}

	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/tree/${pathId}`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('projectResources'), [])];
	}

	return [replace(path('projectResources'), json)];
});

const getLatestCommitInfoCommand = commandFactory(async ({ get, path, payload: { owner, project, pathId = -1 } }) => {
	const projectInfo = get(path('project'));
	console.log(projectInfo);
	if (isEmpty(projectInfo)) {
		return [replace(path('latestCommitInfo'), {})];
	}

	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/latest-commit/${pathId}`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('latestCommitInfo'), {})];
	}

	return [replace(path('latestCommitInfo'), json)];
});

const getProjectReadmeCommand = commandFactory(async ({ get, path, payload: { owner, project } }) => {
	const projectInfo = get(path('project'));
	console.log(projectInfo);
	if (isEmpty(projectInfo)) {
		return [replace(path('readme'), undefined)];
	}

	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/readme`, {
		headers: getHeaders()
	});
	const readmeContent = await response.text();
	if (!response.ok) {
		return [replace(path('readme'), undefined)];
	}

	return [replace(path('readme'), readmeContent)];
});

const getReleaseCountCommand = commandFactory(async ({ get, path, payload: { owner, project } }) => {
	const projectInfo = get(path('project'));
	console.log(projectInfo);
	if (isEmpty(projectInfo)) {
		return [replace(path('releaseCount'), undefined)];
	}

	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/stats/releases`, {
		headers: getHeaders()
	});
	const data = await response.json();
	if (!response.ok) {
		return [replace(path('releaseCount'), undefined)];
	}

	return [replace(path('releaseCount'), data.total)];
});

const getDeployInfoCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/deploy_setting`, {
		headers: getHeaders()
	});
	const userDeployInfo = await response.json();
	if (!response.ok) {
		return [replace(path('userDeployInfo'), undefined)];
	}

	return [replace(path('userDeployInfo'), userDeployInfo)];
});

export const initForNewProjectProcess = createProcess('init-for-new-project', [startInitForNewProjectCommand]);
export const nameInputProcess = createProcess('name-input', [nameInputCommand]);
export const descriptionInputProcess = createProcess('description-input', [descriptionInputCommand]);
export const isPublicInputProcess = createProcess('is-public-input', [isPublicInputCommand]);
export const saveProjectProcess = createProcess('save-project', [saveProjectCommand]);

export const initForViewProjectProcess = createProcess('init-for-view-project', [
	getProjectCommand,
	[getLatestCommitInfoCommand, getProjectResourcesCommand, getProjectReadmeCommand, getReleaseCountCommand]
]);
export const getUserDeployInfoProcess = createProcess('get-user-deploy-info', [getDeployInfoCommand]);
