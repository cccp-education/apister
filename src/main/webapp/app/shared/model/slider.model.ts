import { IUser } from 'app/shared/model/user.model';

export interface ISlider {
  id?: number;
  presentation?: string;
  user?: IUser;
}

export const defaultValue: Readonly<ISlider> = {};
